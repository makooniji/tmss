package cn.iocoder.yudao.module.game.adapter.sports;

import cn.iocoder.yudao.framework.mq.kafka.StatProducer;
import cn.iocoder.yudao.module.system.mapper.GameInfoMapper;
import cn.iocoder.yudao.module.system.mapper.tenant.TenantMapper;
import cn.iocoder.yudao.module.system.records.RecordsMapper;
import cn.iocoder.yudao.module.system.service.VendorTenantService;
import cn.iocoder.yudao.module.system.service.VendorUserService;
import cn.iocoder.yudao.module.game.adapter.BaseAbstractPlatform;
import cn.iocoder.yudao.module.game.manager.DSManager;
import cn.iocoder.yudao.module.game.service.IBalanceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public abstract class AbstractSportPlatform extends BaseAbstractPlatform {
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
    protected VendorTenantService vendorTenantService;

    @Autowired
    protected TenantMapper tenantMapper;

    @Resource
    private DSManager DSManager;

}