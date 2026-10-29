package cn.iocoder.yudao.gateway.controller;


import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.model.game.PlatformResponse;
import cn.iocoder.yudao.framework.log.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.common.enums.ActionEnum;
import cn.iocoder.yudao.framework.common.enums.PlatformCodeEnum;
import cn.iocoder.yudao.framework.common.exception.GameException;
import cn.iocoder.yudao.module.system.model.GameVendorDO;
import cn.iocoder.yudao.module.system.model.game.PlatformRequest;
import cn.iocoder.yudao.module.system.service.VendorService;
import cn.iocoder.yudao.module.system.service.VendorTenantService;
import cn.iocoder.yudao.module.system.service.VendorUserService;
import cn.iocoder.yudao.module.game.adapter.GamingPlatformFactory;
import cn.iocoder.yudao.module.game.adapter.IPlatform;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.log.core.enums.OperateTypeEnum.GATEWAY;

/**
 * 上游厂商-回调通知
 */
@RestController
@Slf4j
@RequestMapping("/gateway")
@RequiredArgsConstructor
public class NotifyController {

    private final VendorTenantService vendorTenantService;

    private final VendorService vendorService;
    private final VendorUserService vendorUserService;
    private final GamingPlatformFactory gamingPlatformFactory;


    /**
     * 体育对接接口
     *
     * @param request
     * @return
     */
    @PostMapping("/notify/sports/{vendorCode}/{*action}")
    @ApiAccessLog(operateType = GATEWAY, operateName = "体育回调")
    public Object sportsNotify(@PathVariable String vendorCode, @PathVariable String action, HttpServletRequest request) {

        vendorCode = vendorCode.toUpperCase();
        Map<String, Object> parse = ServletUtils.getBodyMap(request);
        Map<String, String> header = ServletUtils.getHeaderMap(request);
        Map<String, Object> msgMap = Convert.toMap(String.class, Object.class, parse.getOrDefault("message", new HashMap<>()));
        cn.hutool.json.JSONObject message = JSONUtil.parseObj(msgMap);

        log.info("{} {} 变更余额参数: {}", vendorCode, action, message);

        ActionEnum actionEnum = ActionEnum.from(action);
        if (actionEnum == null) {
            return PlatformResponse.error(PlatformCodeEnum.INVALID_REQUEST);
        }

        if (ObjectUtils.isEmpty(parse)) {
            return PlatformResponse.error(PlatformCodeEnum.INVALID_REQUEST);
        }
        //1. 验证上游厂商
        GameVendorDO vendorTenant = vendorService.getVendorByCode(vendorCode);
        if (ObjectUtil.isEmpty(vendorTenant)) {
            return PlatformResponse.error(PlatformCodeEnum.INVALID_VENDOR);
        }
        if(ObjectUtil.isNotEmpty(vendorTenant.getIpAddress())){
            String clientIP = ServletUtils.getClientIP();
            if (!vendorTenant.getIpAddress().contains(clientIP)) {
                throw new GameException(PlatformCodeEnum.API_IP_BLOCKED);
            }
        }


        IPlatform bean = gamingPlatformFactory.getPlatform(vendorTenant.getAdapter());
        if (ObjectUtil.isEmpty(bean)) {
            log.error("体育回调获取平台失败 vendorCode={}, adapter={}", vendorCode, vendorTenant.getAdapter());
            throw new GameException(PlatformCodeEnum.VENDOR_ERROR);
        }


        PlatformRequest param = new PlatformRequest();
        param.setVendorCode(vendorCode);
        param.setExtraData(msgMap);
        param.setGameVendor(vendorTenant);
        param.setHeader(header);
        param.setAction(action);

        PlatformResponse<Object> execute = bean.forward(actionEnum, param);

        return execute.getData();


    }

    /**
     * 小游戏回调配置
     *
     * @param vendorCode
     * @param action
     * @param request
     * @return
     */
    @PostMapping("/notify/games/{vendorCode}/{*action}")
    @ApiAccessLog(operateType = GATEWAY, operateName = "游戏回调")
    public Object walletNotify(@PathVariable String vendorCode, @PathVariable String action, HttpServletRequest request) {

        vendorCode = vendorCode.toUpperCase();
        Map<String, Object> body = ServletUtils.getBodyMap(request);
        Map<String, String> header = ServletUtils.getHeaderMap(request);


        if (ObjectUtils.isEmpty(body)) {
            return PlatformResponse.error("请求参数错误");
        }

        // DG厂商回调 com_name 作为vendorCode
        if(vendorCode.equals("DG")){
            vendorCode=body.getOrDefault("com_name",vendorCode).toString().toUpperCase();
        }

        //1. 验证上游厂商
        GameVendorDO vendorTenant = vendorService.getVendorByCode(vendorCode);
        if (ObjectUtil.isEmpty(vendorTenant)) {
            return PlatformResponse.error(PlatformCodeEnum.TENANT_ERROR);
        }
        if(ObjectUtil.isNotEmpty(vendorTenant.getIpAddress())){
            String clientIP = ServletUtils.getClientIP();
            if (!vendorTenant.getIpAddress().contains(clientIP)) {
                throw new GameException(PlatformCodeEnum.API_IP_BLOCKED);
            }
        }

        IPlatform bean = gamingPlatformFactory.getPlatform(vendorTenant.getAdapter());
        if (ObjectUtil.isEmpty(bean)) {
            log.error("游戏回调获取平台失败 vendorCode={}, adapter={}", vendorCode, vendorTenant.getAdapter());
            throw new GameException(PlatformCodeEnum.VENDOR_ERROR);
        }

        boolean disable = CommonStatusEnum.isDisable(vendorTenant.getStatus());
        if (disable) {
            throw new GameException(PlatformCodeEnum.UNDER_MAINTENANCE);
        }


        PlatformRequest param = new PlatformRequest();

        param.setVendorCode(vendorCode);
        param.setExtraData(body);
        param.setGameVendor(vendorTenant);
        param.setAction(action);
        param.setHeader(header);

        PlatformResponse<Object> execute = bean.forward(null, param);
        log.info("{} {} 变更余额结果: {}", vendorCode, action, execute.getData());
        return execute.getData();


    }

    /**
     * 多商户配置
     * @param vendorCode
     * @param action
     * @param request
     * @return
     */
    @PostMapping("/notify/multi-agent/{vendorCode}/{*action}")
    @ApiAccessLog(operateType = GATEWAY, operateName = "游戏回调")
    public Object multiVendorNotify(@PathVariable String vendorCode, @PathVariable String action, HttpServletRequest request) {

        vendorCode = vendorCode.toUpperCase();
        String jsonBody = ServletUtils.getBody(request);
        Map<String, Object> body = ServletUtils.getBodyMap(request);
        Map<String, String> header = ServletUtils.getHeaderMap(request);
        Map<String, String> paramMap = ServletUtils.getParamMap(request);
        if (ObjectUtils.isEmpty(body)) {
            return PlatformResponse.error("请求参数错误");
        }


        GameVendorDO vendorTenant=null;
        if(vendorCode.equals("TJ")){
            String mch = paramMap.get("mch");
            vendorTenant= vendorService.getSubAgentByCode(vendorCode,mch);

        }else if(vendorCode.equals("EVO")||vendorCode.equals("EVS")){
            if(action.equals("/check")){
                return MapUtil.builder().put("status", "ok").put("sid", IdUtil.fastUUID())
                        .put("uuid", IdUtil.fastUUID())
                        .build();
            }

            String authToken = paramMap.get("authToken");
            vendorTenant= vendorService.getSubAgentByCode(vendorCode,authToken);
        }else if(vendorCode.equals("DG")){
            // DG厂商回调 com_name 作为vendorCode
            vendorCode=body.getOrDefault("com_name",vendorCode).toString().toUpperCase();
            String mch = paramMap.get("AppID");
            vendorTenant= vendorService.getSubAgentByCode(vendorCode,mch);
        }
        if (ObjectUtil.isEmpty(vendorTenant)) {
            return PlatformResponse.error(PlatformCodeEnum.TENANT_ERROR);
        }
        if(ObjectUtil.isNotEmpty(vendorTenant.getIpAddress())){
            String clientIP = ServletUtils.getClientIP();
            if (!vendorTenant.getIpAddress().contains(clientIP)) {
                throw new GameException(PlatformCodeEnum.API_IP_BLOCKED);
            }
        }
        IPlatform bean = gamingPlatformFactory.getPlatform(vendorTenant.getAdapter());
        if (ObjectUtil.isEmpty(bean)) {
            log.error("游戏回调获取平台失败 vendorCode={}, adapter={}", vendorCode, vendorTenant.getAdapter());
            throw new GameException(PlatformCodeEnum.VENDOR_ERROR);
        }

        boolean disable = CommonStatusEnum.isDisable(vendorTenant.getStatus());
        if (disable) {
            throw new GameException(PlatformCodeEnum.UNDER_MAINTENANCE);
        }


        PlatformRequest param = new PlatformRequest();

        param.setVendorCode(vendorCode);
        param.setExtraData(body);
        param.setGameVendor(vendorTenant);
        param.setAction(action);
        param.setHeader(header);
        param.setParamMap(paramMap);
        param.setBody(jsonBody);

        PlatformResponse<Object> execute = bean.forward(null, param);
        log.info("{} {} 变更余额结果: {}", vendorCode, action, execute.getData());
        return execute.getData();


    }


}
