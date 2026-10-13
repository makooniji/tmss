package cn.iocoder.yudao.module.system.service;

import cn.iocoder.yudao.module.system.mapper.vendoruser.UserFlowMapper;
import cn.iocoder.yudao.module.system.model.VendorUserFlowDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VendorUserFlowServiceImpl implements VendorUserFlowService {

    @Resource
    private UserFlowMapper vendorUserFlowMapper;

    /**
     * 开启新事务保存流水，不受上层业务事务回滚的影响
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFlowRequiresNew(VendorUserFlowDO flowDO) {
        vendorUserFlowMapper.insert(flowDO);
    }

    @Override
    public VendorUserFlowDO getTransferFlow(String refId, String username, String currency) {
        LambdaQueryWrapper<VendorUserFlowDO> queryWrapper = Wrappers.lambdaQuery(VendorUserFlowDO.class)
                .eq(VendorUserFlowDO::getReferenceId, refId)
                .eq(VendorUserFlowDO::getUsername, username)
                .eq(VendorUserFlowDO::getCurrency, currency);
        return vendorUserFlowMapper.selectOne(queryWrapper);
    }
}