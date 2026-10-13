package cn.iocoder.yudao.gateway.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.PlatformCodeEnum;
import cn.iocoder.yudao.framework.common.exception.GameException;
import cn.iocoder.yudao.framework.common.model.game.GameApiRequest;
import cn.iocoder.yudao.framework.common.model.game.PlatformResponse;
import cn.iocoder.yudao.framework.common.model.game.push.DSRollback;
import cn.iocoder.yudao.framework.common.util.SignUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.gateway.GMGatewayApp;
import cn.iocoder.yudao.framework.common.enums.ActionEnum;
import cn.iocoder.yudao.module.game.convert.GameRecordConvert;
import cn.iocoder.yudao.module.game.service.IBalanceService;
import cn.iocoder.yudao.module.system.model.GameInfoDO;
import cn.iocoder.yudao.module.system.model.GameVendorDO;
import cn.iocoder.yudao.module.system.model.VendorUserDO;
import cn.iocoder.yudao.module.system.model.dto.GameStaticDTO;
import cn.iocoder.yudao.module.system.model.game.PlatformRequest;
import cn.iocoder.yudao.module.system.model.tenant.TenantDO;
import cn.iocoder.yudao.module.system.service.*;
import cn.iocoder.yudao.module.game.adapter.GamingPlatformFactory;
import cn.iocoder.yudao.module.game.adapter.IPlatform;
import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@SpringBootTest(classes = GMGatewayApp.class)
@Slf4j
@ActiveProfiles("local")
@RunWith(SpringRunner.class)
public class NotifyControllerTest {

    @Autowired
    private VendorService vendorService;
    @Autowired
    private  GamingPlatformFactory gamingPlatformFactory;

    @Autowired
    private  VendorTenantService vendorTenantService;


    @Autowired
    private  GameInfoService gameInfoService;
    @Autowired
    private  VendorUserService vendorUserService;
    @Autowired
    private  cn.iocoder.yudao.module.system.service.TenantService tenantService;
    @Autowired
    private  GameRecordsService gameRecordsService;
    @Autowired
    private  GameRecordConvert convert;
    @Autowired
    private  IBalanceService balanceService;
    @Autowired
    private  GameCategoryService gameCategoryService;



    @Test
    public void sportsNotify() {
        String body="{\"key\":\"vqnztotn0d\",\"message\":{\"action\":\"Settle\",\"txns\":[{\"userId\":\"IG001BB98_devpa10510\",\"refId\":\"4501400_6495264_M\",\"txId\":322023095992277,\"updateTime\":\"2026-04-09T01:05:18.214-04:00\",\"winlostDate\":\"2026-04-09T00:00:00.000-04:00\",\"status\":\"half lose\",\"payout\":4.75,\"creditAmount\":4.75,\"debitAmount\":0.0,\"extraStatus\":\"\",\"settlementTime\":\"2026-04-09T01:05:18.214\"}],\"operationId\":\"4501400_4_4313178_M\"}}";

       String vendorCode = "SABA";
        String action="/Settle";
        Map parse = JsonUtils.parseObject(body, Map.class);

        Map<String, Object> msgMap = Convert.toMap(String.class, Object.class, parse.getOrDefault("message", new HashMap<>()));
        cn.hutool.json.JSONObject message = JSONUtil.parseObj(msgMap);

        log.info("{} {} 变更余额参数: {}", vendorCode, action, message);

        ActionEnum actionEnum = ActionEnum.from(action);

        //1. 验证上游厂商
        GameVendorDO vendorTenant = vendorService.getVendorByCode(vendorCode);
        IPlatform bean = gamingPlatformFactory.getPlatform(vendorTenant.getAdapter());




        PlatformRequest param = new PlatformRequest();
        param.setVendorCode(vendorCode);
        param.setExtraData(msgMap);
        param.setGameVendor(vendorTenant);
        param.setHeader(null);
        param.setAction(action);

        PlatformResponse execute = bean.forward(actionEnum, param);

        log.info("返回结果 {}",execute.getData());
    }

    @Test
    public void testConvert() {
        DSRollback dsRollback = new DSRollback();
        dsRollback.setBetId(IdUtil.simpleUUID());
        dsRollback.setCreditAmount(BigDecimal.valueOf(100));
        Map<String, Object> body = JsonUtils.convertObject(dsRollback, new TypeReference<LinkedHashMap<String, Object>>() {
        });

        log.info("{}", body);
    }
    private TenantDO authenticateTenant(String rawJson, String sign, String key) {
        TenantDO tenant = TenantUtils.executeIgnore(() -> tenantService.getTenantByKey(key));
        if (ObjectUtil.isEmpty(tenant)) {
            throw new GameException(PlatformCodeEnum.TENANT_ERROR);
        }
        if (CommonStatusEnum.isDisable(tenant.getStatus())) {
            throw new GameException(PlatformCodeEnum.TENANT_DISABLED);
        }
        if (!SignUtils.verifySign(rawJson, tenant.getSecretKey(), sign)) {
            throw new GameException(PlatformCodeEnum.INVALID_SIGNATURE);
        }
        if (ObjectUtil.isNotEmpty(tenant.getIpAddress())) {
            String clientIP = ServletUtils.getClientIP();
            if (!tenant.getIpAddress().contains(clientIP)) {
                throw new GameException(PlatformCodeEnum.API_IP_BLOCKED);
            }
        }
        return tenant;
    }

    @Test
    public void testGetUrl() {
        GameApiRequest data=new GameApiRequest();
        data.setBalance(BigDecimal.ZERO);
        data.setCurrency("BRL");
        data.setLobbyUrl("https://www.baidu.com");
        data.setUsername("test11");
        data.setTraceId(IdUtil.simpleUUID());
        data.setPlatform("web");
        data.setGameCode("1005");
        data.setIpAddress("175.136.215.138");

        String rawJson = JsonUtils.toJsonString(data);
        String key="zfnLjVYcEpAcP4Qw21xkY";
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(data);
        String sign=SignUtils.buildSign(stringObjectMap,key);
        // 校验商户信息
        TenantDO currentTenant = authenticateTenant(rawJson, sign, key);
        //4. 验证下游是否开通当前货币
        GameApiRequest body = JSONObject.parseObject(rawJson, GameApiRequest.class);
        if (ObjectUtil.isNotEmpty(currentTenant.getCurrency())) {
            if (!currentTenant.getCurrency().contains(body.getCurrency())) {
                throw new GameException(PlatformCodeEnum.CURRENCY_NOT_SUPPORTED);
            }
        }
        TenantUtils.execute(currentTenant.getId(), () -> {


            if (currentTenant.getImplType().equals(1)) {
                if (ObjectUtil.isEmpty(body.getBalance())) {
                    throw new GameException(PlatformCodeEnum.INVALID_BALANCE);
                }
            }
            //5. 查询游戏数据
            GameInfoDO game = gameInfoService.getInfoByCode(body.getGameCode());
            if (ObjectUtil.isEmpty(game)) {
                throw new GameException(PlatformCodeEnum.INVALID_GAME);
            }
            //6. 验证下游是否开通当前厂商和游戏
            if (CollectionUtils.isNotEmpty(currentTenant.getVendorCodes())
                    && !currentTenant.getVendorCodes().contains(game.getVendorCode())) {
                throw new GameException(PlatformCodeEnum.VENDOR_PLATFORM_NOT_SUPPORTED);
            }
            //7. 验证游戏状态
            if (!CommonStatusEnum.isEnable(game.getStatus())) {
                throw new GameException(PlatformCodeEnum.GAME_DISABLED);
            }
            if (!CommonStatusEnum.isEnable(game.getMaintain())) {
                throw new GameException(PlatformCodeEnum.UNDER_MAINTENANCE);
            }
            //8. 验证游戏支持的货币
            if (ObjectUtil.isNotEmpty(game.getSupportCurrency())) {
                if (!game.getSupportCurrency().contains(body.getCurrency())) {
                    throw new GameException(PlatformCodeEnum.GAME_CURRENCY_NOT_SUPPORTED);
                }
            }

            //9. 验证上游厂商
            GameVendorDO gameVendor = vendorService.getVendorByCode(game.getVendorCode());
            if (ObjectUtil.isEmpty(gameVendor)) {
                throw new GameException(PlatformCodeEnum.VENDOR_ERROR);
            }



            IPlatform bean = gamingPlatformFactory.getPlatform(gameVendor.getAdapter());
            if (ObjectUtil.isEmpty(bean)) {
                throw new GameException(PlatformCodeEnum.VENDOR_ERROR);
            }

            String currency = body.getCurrency();
            if (ObjectUtil.isNotEmpty(gameVendor.getCurrency())) {
                if (!gameVendor.getCurrency().contains(currency)) {
                    throw new GameException(PlatformCodeEnum.GAME_CURRENCY_NOT_SUPPORTED);
                }
            }
            // 多货币厂商适配
            if(ObjectUtil.isNotEmpty(gameVendor.getSubVendors())){
                gameVendor= vendorService.getSubAgent(gameVendor.getVendorCode(),currency);
            }
            //10. 查询会员账号
            VendorUserDO gameUser = vendorUserService.getUser(body.getUsername(), game.getVendorCode());
            if (ObjectUtil.isEmpty(gameUser)) {
                PlatformResponse<String> response = bean.createMember(gameVendor, body);
                gameUser = vendorUserService.makeUser(currency, gameVendor, currentTenant, body.getUsername(), response.getData());

            }
            // 11重构用户名已平台定义为准
            body.setUsername(gameUser.getVendorUsername());
            GameStaticDTO.buildOpen(gameUser, gameVendor, game,body.getCurrency());

            //12. 调用上游返回链接
            PlatformResponse<String> gameLink = bean.getGameLink(gameVendor, body, gameUser);
            String data1 = gameLink.getData();
            log.info("data1:{}", URLUtil.decode(data1));
        });
    }
}