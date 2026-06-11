package cn.iocoder.yudao.gateway.controller;


import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.gateway.GMGatewayApp;
import cn.iocoder.yudao.module.game.adapter.GamingPlatformFactory;
import cn.iocoder.yudao.module.game.adapter.IPlatform;
import cn.iocoder.yudao.module.system.dal.dataobject.vendor.GameVendorDO;
import cn.iocoder.yudao.module.system.enums.ActionEnum;
import cn.iocoder.yudao.module.system.model.api.PlatformRequest;
import cn.iocoder.yudao.module.system.model.api.PlatformResponse;
import cn.iocoder.yudao.module.system.model.api.push.DSRollback;
import cn.iocoder.yudao.module.system.service.vendor.VendorService;
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
    private  VendorService vendorService;
    @Autowired
    private  GamingPlatformFactory gamingPlatformFactory;
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
}