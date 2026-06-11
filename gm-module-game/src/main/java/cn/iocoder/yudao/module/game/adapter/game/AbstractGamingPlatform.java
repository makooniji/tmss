package cn.iocoder.yudao.module.game.adapter.game;

import cn.iocoder.yudao.framework.mq.kafka.StatProducer;
import cn.iocoder.yudao.module.game.adapter.BaseAbstractPlatform;
import cn.iocoder.yudao.module.game.manager.DSManager;
import cn.iocoder.yudao.module.game.service.IBalanceService;
import cn.iocoder.yudao.module.system.dal.mysql.info.GameInfoMapper;
import cn.iocoder.yudao.module.system.dal.mysql.records.RecordsMapper;
import cn.iocoder.yudao.module.system.dal.mysql.tenant.TenantMapper;
import cn.iocoder.yudao.module.system.service.vendortenant.VendorTenantService;
import cn.iocoder.yudao.module.system.service.vendoruser.VendorUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public abstract class AbstractGamingPlatform extends BaseAbstractPlatform {
    @Resource
    protected GameInfoMapper gameInfoMapper;
    @Resource
    protected StatProducer statProducer;
    @Autowired
    protected IBalanceService balanceService;
    @Autowired
    protected VendorUserService userService;
    @Autowired
    protected RecordsMapper recordsMapper;
    @Autowired
    protected WebClient webClient;

    @Autowired
    protected VendorTenantService vendorTenantService;

    @Autowired
    protected TenantMapper tenantMapper;

    @Resource
    private DSManager DSManager;




}