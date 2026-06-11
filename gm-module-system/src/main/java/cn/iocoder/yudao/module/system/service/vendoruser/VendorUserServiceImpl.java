package cn.iocoder.yudao.module.system.service.vendoruser;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.controller.admin.vendoruser.vo.VendorUserPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.vendoruser.vo.VendorUserSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendor.GameVendorDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendoruser.VendorUserBalanceDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendoruser.VendorUserDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendoruser.VendorUserFlowDO;
import cn.iocoder.yudao.module.system.dal.mysql.tenant.TenantMapper;
import cn.iocoder.yudao.module.system.dal.mysql.vendoruser.UserBalanceMapper;
import cn.iocoder.yudao.module.system.dal.mysql.vendoruser.UserFlowMapper;
import cn.iocoder.yudao.module.system.dal.mysql.vendoruser.VendorUserMapper;
import cn.iocoder.yudao.module.system.dal.redis.RedisKeyConstants;
import cn.iocoder.yudao.module.system.enums.PlatformCodeEnum;
import cn.iocoder.yudao.module.system.exception.GameException;
import cn.iocoder.yudao.module.system.model.api.PlatformResponse;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.VENDOR_USER_NOT_EXISTS;


/**
 * 厂商玩家 Service 实现类
 *
 * @author osca
 */
@Service
@Validated
@Slf4j
public class VendorUserServiceImpl implements VendorUserService {

    @Resource
    private VendorUserMapper vendorUserMapper;
    @Resource
    private UserBalanceMapper userBalanceMapper;
    @Resource
    private TenantMapper tenantMapper;
    @Resource
    private UserFlowMapper userFlowMapper;

    @Override
    public Long createVendorUser(VendorUserSaveReqVO createReqVO) {
        // 插入
        VendorUserDO vendorUser = BeanUtils.toBean(createReqVO, VendorUserDO.class);
        vendorUserMapper.insert(vendorUser);

        // 返回
        return vendorUser.getId();
    }

    @Override
    public void updateVendorUser(VendorUserSaveReqVO updateReqVO) {
        // 校验存在
        validateVendorUserExists(updateReqVO.getId());
        // 更新
        VendorUserDO updateObj = BeanUtils.toBean(updateReqVO, VendorUserDO.class);
        vendorUserMapper.updateById(updateObj);
    }

    @Override
    public void deleteVendorUser(Long id) {
        // 校验存在
        validateVendorUserExists(id);
        // 删除
        vendorUserMapper.deleteById(id);
    }

    @Override
        public void deleteVendorUserListByIds(List<Long> ids) {
        // 删除
        vendorUserMapper.deleteByIds(ids);
        }


    private void validateVendorUserExists(Long id) {
        if (vendorUserMapper.selectById(id) == null) {
            throw exception(VENDOR_USER_NOT_EXISTS);
        }
    }

    @Override
    public VendorUserDO getUserByVendor(Long id) {
        return vendorUserMapper.selectById(id);
    }

    @Override
    public PageResult<VendorUserDO> getVendorUserPage(VendorUserPageReqVO pageReqVO) {
        return vendorUserMapper.selectPage(pageReqVO);
    }

    @Override
    public void opsBalance(String username, BigDecimal changeMoney, String currency) {
        userBalanceMapper.opsBalance(username, changeMoney, currency);
    }

    @Override
    @CachePut(value = RedisKeyConstants.USERINFO, key = "#username", unless = "#result==null")
    @Transactional(rollbackFor = Exception.class)
    public VendorUserDO makeUser(String currency, GameVendorDO gameVendor, TenantDO tenant, String username, String vendorUsername) {
        VendorUserDO vendorUser =new VendorUserDO();
        vendorUser.setVendorId(gameVendor.getId());
        vendorUser.setVendorCode(gameVendor.getVendorCode());
        vendorUser.setUsername(username);
        vendorUser.setVendorUsername(vendorUsername);
        vendorUser.setStatus(0);
        vendorUser.setTenantId(tenant.getId());
        vendorUser.setTenantCode(tenant.getCode());
        vendorUser.setTenantName(tenant.getName());
        vendorUserMapper.insertIgnore(vendorUser);
        userBalanceMapper.insertIgnore(VendorUserBalanceDO.builder()
                .username(username)
                .currency(currency)
                .balance(BigDecimal.ZERO)
                .build());
        //更新租户会员数量
        tenantMapper.update(Wrappers.<TenantDO>lambdaUpdate().eq(TenantDO::getId, tenant.getId())
                .setSql("account_count=account_count+1")
        );
        return vendorUser;
    }

    @Override
    @Cacheable(value = RedisKeyConstants.USERINFO, key = "#username", unless = "#result == null")
    public VendorUserDO getUser(String username) {
        return vendorUserMapper.selectOne("username", username);
    }

    @Override
    public VendorUserDO getUser(String username, String vendorCode) {
        return vendorUserMapper.selectOne("username", username, "vendor_code", vendorCode);
    }

    @Override
    public VendorUserDO getUserByVendor(String username)
    {
        return vendorUserMapper.selectOne("vendor_username", username);
    }

    public VendorUserDO getUserByVendor(String username, String vendorCode)
    {
        return vendorUserMapper.selectOne("vendor_username", username, "vendor_code", vendorCode);
    }

    @Override
    public void asyncBalance(String username, BigDecimal amount, String currency) {
        userBalanceMapper.update(Wrappers.<VendorUserBalanceDO>lambdaUpdate()
                .eq(VendorUserBalanceDO::getUsername, username)
                .eq(VendorUserBalanceDO::getCurrency, currency)
                .setSql("balance = " + amount));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VendorUserFlowDO depositAmount(String refId, String username, BigDecimal changeAmount, String currency) {
        VendorUserBalanceDO vendorUserBalanceDO = userBalanceMapper.selectOne(Wrappers.<VendorUserBalanceDO>lambdaQuery()
                .eq(VendorUserBalanceDO::getUsername, username)
                .eq(VendorUserBalanceDO::getCurrency, currency)
        );
        BigDecimal beforeAmount = BigDecimal.ZERO;
        BigDecimal afterAmount = BigDecimal.ZERO;
        if (ObjectUtil.isEmpty(vendorUserBalanceDO)) {
            userBalanceMapper.insert(VendorUserBalanceDO.builder()
                    .username(username)
                    .currency(currency)
                    .balance(changeAmount)
                    .build());
            afterAmount = changeAmount;
        } else {
            beforeAmount = vendorUserBalanceDO.getBalance();
            userBalanceMapper.update(Wrappers.<VendorUserBalanceDO>lambdaUpdate()
                    .eq(VendorUserBalanceDO::getUsername, username)
                    .eq(VendorUserBalanceDO::getCurrency, currency)
                    .setSql("balance = balance + " + changeAmount));
            afterAmount = beforeAmount.add(changeAmount);
        }
        // 添加转账记录
        VendorUserFlowDO userFlowDO = VendorUserFlowDO.builder()
                .username(username)
                .currency(currency)
                .amount(changeAmount)
                .beforeAmount(beforeAmount)
                .afterAmount(afterAmount)
                .referenceId(refId)
                .build();
        userFlowMapper.insert(userFlowDO);
        return userFlowDO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VendorUserFlowDO withdrawAmount(String refId, String username, BigDecimal changeAmount, String currency) {
        VendorUserBalanceDO vendorUserBalanceDO = userBalanceMapper.selectOne(Wrappers.<VendorUserBalanceDO>lambdaQuery()
                .eq(VendorUserBalanceDO::getUsername, username)
                .eq(VendorUserBalanceDO::getCurrency, currency)
                .ge(VendorUserBalanceDO::getBalance, changeAmount.abs())
        );

        if (ObjectUtil.isEmpty(vendorUserBalanceDO)) {
            throw new GameException(PlatformCodeEnum.INSUFFICIENT_FUNDS);
        }
        BigDecimal beforeAmount = vendorUserBalanceDO.getBalance();
        userBalanceMapper.update(Wrappers.<VendorUserBalanceDO>lambdaUpdate()
                .eq(VendorUserBalanceDO::getUsername, username)
                .eq(VendorUserBalanceDO::getCurrency, currency)
                .setSql("balance = balance + " + changeAmount));
        BigDecimal afterAmount = beforeAmount.add(changeAmount);
        // 添加转账记录
        VendorUserFlowDO userFlowDO = VendorUserFlowDO.builder()
                .username(username)
                .currency(currency)
                .amount(changeAmount)
                .beforeAmount(beforeAmount)
                .afterAmount(afterAmount)
                .referenceId(refId)
                .build();
        userFlowMapper.insert(userFlowDO);
        return userFlowDO;
    }

    @Override
    public VendorUserBalanceDO getBalance(String username, String currency) {
        return userBalanceMapper.selectOne(Wrappers.<VendorUserBalanceDO>lambdaQuery()
                .eq(VendorUserBalanceDO::getUsername, username)
                .eq(VendorUserBalanceDO::getCurrency, currency));
    }

    @Override
    public VendorUserDO getUserByUsername(String username) {
        if (ObjectUtils.isEmpty(username)) {
            log.error("oneAPI回调请求用户名不存在：{}", username);
            throw new GameException(PlatformCodeEnum.USER_NOT_EXISTS);
        }
        VendorUserDO userByVendor = this.getUserByVendor(username);
        if (ObjectUtils.isEmpty(userByVendor)) {
            log.error("oneAPI回调请求用户不存在：{}", username);
            throw new GameException(PlatformCodeEnum.USER_NOT_EXISTS);
        }
        return userByVendor;
    }

}