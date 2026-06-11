package cn.iocoder.yudao.module.game.service;

import cn.iocoder.yudao.framework.common.util.RedisUtils;
import cn.iocoder.yudao.module.system.dal.dataobject.vendoruser.VendorUserBalanceDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendoruser.VendorUserFlowDO;
import cn.iocoder.yudao.module.system.service.vendoruser.VendorUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class BalanceServiceImpl implements IBalanceService {


    private final VendorUserService vendorUserService;


    @Override
    @Transactional
    public void updateBalance(String username, BigDecimal changeAmount, String currency) {
        String kerKey = "balance:" + username;
        RedisUtils.executeWithLock(kerKey, () -> vendorUserService.opsBalance(username, changeAmount, currency));
    }

    @Override
    public void asyncBalance(String username, BigDecimal amount, String currency) {
        String kerKey = "balance:" + username;
        RedisUtils.executeWithLock(kerKey, () -> vendorUserService.asyncBalance(username, amount, currency));
    }

    @Override
    public VendorUserFlowDO transferAmount(String refId, String username, BigDecimal changeAmount, String currency) {
        String kerKey = "balance:" + username;
        if (changeAmount.doubleValue() < 0) {
            return RedisUtils.executeWithLock(kerKey, () -> vendorUserService.withdrawAmount(refId, username, changeAmount.abs(), currency));
        }

        return RedisUtils.executeWithLock(kerKey, () -> vendorUserService.depositAmount(refId, username, changeAmount, currency));
    }

    @Override
    public VendorUserBalanceDO getBalance(String username, String currency) {
        return vendorUserService.getBalance(username, currency);
    }
}
