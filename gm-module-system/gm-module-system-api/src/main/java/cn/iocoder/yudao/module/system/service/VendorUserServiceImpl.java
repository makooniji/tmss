package cn.iocoder.yudao.module.system.service;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.exception.GameException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.enums.RedisKeyConstants;
import cn.iocoder.yudao.module.system.mapper.vendoruser.UserBalanceMapper;
import cn.iocoder.yudao.module.system.mapper.vendoruser.UserFlowMapper;
import cn.iocoder.yudao.module.system.mapper.vendoruser.VendorUserMapper;
import cn.iocoder.yudao.module.system.model.tenant.TenantDO;
import cn.iocoder.yudao.module.system.model.GameVendorDO;
import cn.iocoder.yudao.module.system.model.VendorUserBalanceDO;
import cn.iocoder.yudao.module.system.model.VendorUserDO;
import cn.iocoder.yudao.module.system.model.VendorUserFlowDO;
import cn.iocoder.yudao.module.system.mapper.tenant.TenantMapper;
import cn.iocoder.yudao.framework.common.enums.PlatformCodeEnum;

import cn.iocoder.yudao.module.system.model.vo.VendorUserPageReqVO;
import cn.iocoder.yudao.module.system.model.vo.VendorUserSaveReqVO;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.VENDOR_USER_NOT_EXISTS;



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
    private  VendorUserFlowService vendorUserFlowService;
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
    public void updateVendorUserStatus(Long id, Integer status) {
        validateVendorUserExists(id);
        VendorUserDO updateObj = new VendorUserDO();
        updateObj.setId(id);
        updateObj.setStatus(status);
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
        if(gameVendor!=null) {
            vendorUser.setVendorId(gameVendor.getId());
            vendorUser.setVendorCode(gameVendor.getVendorCode());
        }
        vendorUser.setUsername(username);
        vendorUser.setVendorUsername(vendorUsername);
        vendorUser.setStatus(0);
        vendorUser.setTenantId(tenant.getId());
        vendorUser.setTenantCode(tenant.getCode());
        vendorUser.setTenantName(tenant.getName());

        boolean exists = vendorUserMapper.exists(Wrappers.<VendorUserDO>lambdaQuery()
                .eq(VendorUserDO::getUsername, username)
                .eq(VendorUserDO::getTenantId, tenant.getId())
        );

        int inserted = vendorUserMapper.insertIgnore(vendorUser);
        userBalanceMapper.insertIgnore(VendorUserBalanceDO.builder()
                .username(username)
                .currency(currency)
                .balance(BigDecimal.ZERO)
                .build());
        // 仅真正插入会员时计数；冲突忽略时不涨 account_count，并回查已存在记录
        if (inserted > 0 && !exists) {
            tenantMapper.update(Wrappers.<TenantDO>lambdaUpdate().eq(TenantDO::getId, tenant.getId())
                    .setSql("account_count=account_count+1")
            );
        }
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
    public VendorUserDO getUserByCurrency(String username, String currency) {
        return vendorUserMapper.selectOne("username", username, "currency", currency);
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
        // 1. 查询当前用户余额
        VendorUserBalanceDO vendorUserBalanceDO = userBalanceMapper.selectOne(Wrappers.<VendorUserBalanceDO>lambdaQuery()
                .eq(VendorUserBalanceDO::getUsername, username)
                .eq(VendorUserBalanceDO::getCurrency, currency)
        );

        BigDecimal beforeAmount = (vendorUserBalanceDO != null) ? vendorUserBalanceDO.getBalance() : BigDecimal.ZERO;

        // 2. 检查用户是否存在或余额是否足够
        boolean isInsufficient = vendorUserBalanceDO == null
                || beforeAmount.add(changeAmount).compareTo(BigDecimal.ZERO) < 0;

        if (isInsufficient) {
            // 记录失败流水（余额不足）
            VendorUserFlowDO failFlow = VendorUserFlowDO.builder()
                    .username(username)
                    .currency(currency)
                    .amount(changeAmount)
                    .beforeAmount(beforeAmount)
                    .afterAmount(beforeAmount) // 变动后余额保持不变
                    .referenceId(refId)
                    .status(VendorUserFlowDO.FlowStatusEnum.FAIL) // 2-失败/余额不足
                    .remark("INSUFFICIENT_FUNDS")
                    .build();

            // 使用 REQUIRES_NEW 事务插入，防止抛出异常导致失败流水被回滚
            vendorUserFlowService.saveFlowRequiresNew(failFlow);

            throw new GameException(PlatformCodeEnum.INSUFFICIENT_FUNDS);
        }

        // 3. 执行数据库级别的原子更新（带条件过滤，彻底解决并发超扣问题）
        int updateRows = userBalanceMapper.update(null, Wrappers.<VendorUserBalanceDO>lambdaUpdate()
                .eq(VendorUserBalanceDO::getUsername, username)
                .eq(VendorUserBalanceDO::getCurrency, currency)
                .apply("balance + {0} >= 0", changeAmount) // 数据库层校验确保余额非负
                .setSql("balance = balance + {0}", changeAmount) // 参数化绑定，防止 SQL 注入
        );

        // 4. 并发情况下原子更新失败（更新 0 行）
        if (updateRows == 0) {
            VendorUserFlowDO failFlow = VendorUserFlowDO.builder()
                    .username(username)
                    .currency(currency)
                    .amount(changeAmount)
                    .beforeAmount(beforeAmount)
                    .afterAmount(beforeAmount)
                    .referenceId(refId)
                    .status(VendorUserFlowDO.FlowStatusEnum.FAIL) // 2-失败/并发扣款失败
                    .remark("INTERNAL_ERROR")
                    .build();

            vendorUserFlowService.saveFlowRequiresNew(failFlow);

            throw new GameException(PlatformCodeEnum.INTERNAL_ERROR);
        }

        // 5. 更新成功，插入成功流水
        BigDecimal afterAmount = beforeAmount.add(changeAmount);
        VendorUserFlowDO successFlow = VendorUserFlowDO.builder()
                .username(username)
                .currency(currency)
                .amount(changeAmount)
                .beforeAmount(beforeAmount)
                .afterAmount(afterAmount)
                .referenceId(refId)
                .status(VendorUserFlowDO.FlowStatusEnum.SUCCESS) // 1-成功
                .remark("SUCCESS")
                .build();

        userFlowMapper.insert(successFlow);
        return successFlow;
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