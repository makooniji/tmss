package cn.iocoder.yudao.module.game.service;


import cn.iocoder.yudao.module.system.dal.dataobject.vendoruser.VendorUserBalanceDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendoruser.VendorUserFlowDO;

import java.math.BigDecimal;

/**
 * 处理游戏记录和用户余额相关的Service
 */
public interface IBalanceService {

    /**
     * 更新账户余额
     *
     * @param username
     * @param changeAmount
     * @param currency
     */
    void updateBalance(String username, BigDecimal changeAmount, String currency);

    void asyncBalance(String username, BigDecimal amount, String currency);

    VendorUserFlowDO transferAmount(String refId, String username, BigDecimal changeAmount, String currency);

    VendorUserBalanceDO getBalance(String username, String currency);
}
