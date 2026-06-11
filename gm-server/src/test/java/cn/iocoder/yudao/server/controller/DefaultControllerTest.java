package cn.iocoder.yudao.server.controller;


import cn.iocoder.yudao.module.system.dal.mysql.tenant.TenantMapper;
import cn.iocoder.yudao.server.GMServerApp;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

@SpringBootTest(classes = GMServerApp.class)
@ContextConfiguration(classes = GMServerApp.class)
@ActiveProfiles("local")
@RunWith(SpringRunner.class)
@Slf4j
public class DefaultControllerTest {


    @Resource
    private TenantMapper tenantMapper;
    @Test
    public void  testMap(){
        List<Map<String, Object>> maps = tenantMapper.selectTenantStatus(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO>()
                        .gt(cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO::getId, 1)
                        .apply("deleted = 0"));
        log.info("{}",maps);
    }
}