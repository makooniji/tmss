package cn.iocoder.yudao.module.system.service;

import cn.iocoder.yudao.module.system.mapper.vendoruser.UserFlowMapper;
import cn.iocoder.yudao.module.system.model.VendorUserFlowDO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


public interface VendorUserFlowService{


    void saveFlowRequiresNew(VendorUserFlowDO flowDO);

    VendorUserFlowDO getTransferFlow(String refId, String username, String currency);
}