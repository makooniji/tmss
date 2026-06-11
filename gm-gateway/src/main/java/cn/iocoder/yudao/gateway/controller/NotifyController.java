package cn.iocoder.yudao.gateway.controller;


import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.game.adapter.GamingPlatformFactory;
import cn.iocoder.yudao.module.game.adapter.IPlatform;
import cn.iocoder.yudao.module.game.adapter.sports.saba.config.GzipRequestWrapper;
import cn.iocoder.yudao.module.system.dal.dataobject.vendor.GameVendorDO;
import cn.iocoder.yudao.module.system.enums.ActionEnum;
import cn.iocoder.yudao.module.system.enums.PlatformCodeEnum;
import cn.iocoder.yudao.module.system.exception.GameException;
import cn.iocoder.yudao.module.system.model.api.PlatformRequest;
import cn.iocoder.yudao.module.system.model.api.PlatformResponse;
import cn.iocoder.yudao.module.system.service.vendor.VendorService;
import cn.iocoder.yudao.module.system.service.vendortenant.VendorTenantService;
import cn.iocoder.yudao.module.system.service.vendoruser.VendorUserService;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.GATEWAY;

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

    public static Map<String, Object> parse(HttpServletRequest request) {

        Map<String, Object> result = new LinkedHashMap<>();

        try {

            String contentType = request.getContentType();

            log.info("SABA 请求方式:{},{}", contentType, request.getHeader("Content-Encoding"));

            // JSON
            if (contentType != null && contentType.contains("application/json")) {
                String body;

                if (request instanceof GzipRequestWrapper) {
                    body = ((GzipRequestWrapper) request).getBodyString();
                } else {
                    body = IoUtil.read(request.getInputStream(), StandardCharsets.UTF_8);
                }
                log.info("SABA body={}", body);
                if (body != null && !body.isEmpty()) {
                    Map<String, Object> map = JsonUtils.parseObject(body, new TypeReference<LinkedHashMap<String, Object>>() {
                    });
                    result.putAll(map);
                }

            }
            // FORM
            else if (contentType != null &&
                    contentType.contains("application/x-www-form-urlencoded")) {

                Map<String, String[]> params = request.getParameterMap();

                params.forEach((k, v) -> {
                    if (v != null && v.length == 1) {
                        result.put(k, v[0]);
                    } else {
                        result.put(k, v);
                    }
                });

            }

            // fallback 读取 query/form
            Map<String, String[]> params = request.getParameterMap();
            // 判断 params 是否有值
            if (params != null && !params.isEmpty()) {
                params.forEach((k, v) -> {
                    if (v != null && v.length == 1) {
                        result.put(k, v[0]); // 如果只有一个值，则直接放入 result
                    } else {
                        result.put(k, v); // 否则，放入数组 v
                    }
                });
            }



        } catch (Exception e) {
            throw new RuntimeException("request parse error", e);
        }

        return result;
    }

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
        Map<String, Object> parse = parse(request);
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


        IPlatform bean = gamingPlatformFactory.getPlatform(vendorTenant.getAdapter());


        PlatformRequest param = new PlatformRequest();
        param.setVendorCode(vendorCode);
        param.setExtraData(msgMap);
        param.setGameVendor(vendorTenant);
        param.setHeader(header);
        param.setAction(action);

        PlatformResponse execute = bean.forward(actionEnum, param);

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
        Map<String, Object> body = parse(request);
        Map<String, String> header = ServletUtils.getHeaderMap(request);
        if (ObjectUtils.isEmpty(body)) {
            return PlatformResponse.error("请求参数错误");
        }

        //1. 验证上游厂商
        GameVendorDO vendorTenant = vendorService.getVendorByCode(vendorCode);
        if (ObjectUtil.isEmpty(vendorTenant)) {
            return PlatformResponse.error(PlatformCodeEnum.TENANT_ERROR);
        }
        IPlatform bean = gamingPlatformFactory.getPlatform(vendorTenant.getAdapter());

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

        PlatformResponse execute = bean.forward(null, param);
        log.info("{} {} 变更余额结果: {}", vendorCode, action, execute.getData());
        return execute.getData();


    }


}
