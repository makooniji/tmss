package cn.iocoder.yudao.module.system.service.vendoruser;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.vendoruser.vo.VendorUserPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.vendoruser.vo.VendorUserSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendor.GameVendorDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendoruser.VendorUserBalanceDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendoruser.VendorUserDO;
import cn.iocoder.yudao.module.system.dal.dataobject.vendoruser.VendorUserFlowDO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.util.List;

/**
 * 厂商玩家 Service 接口
 *
 * @author osca
 */
public interface VendorUserService {

    /**
     * 创建厂商玩家
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createVendorUser(@Valid VendorUserSaveReqVO createReqVO);

    /**
     * 更新厂商玩家
     *
     * @param updateReqVO 更新信息
     */
    void updateVendorUser(@Valid VendorUserSaveReqVO updateReqVO);

    /**
     * 删除厂商玩家
     *
     * @param id 编号
     */
    void deleteVendorUser(Long id);

    /**
    * 批量删除厂商玩家
    *
    * @param ids 编号
    */
    void deleteVendorUserListByIds(List<Long> ids);

    /**
     * 获得厂商玩家
     *
     * @param id 编号
     * @return 厂商玩家
     */
    VendorUserDO getUserByVendor(Long id);

    /**
     * 获得厂商玩家分页
     *
     * @param pageReqVO 分页查询
     * @return 厂商玩家分页
     */
    PageResult<VendorUserDO> getVendorUserPage(VendorUserPageReqVO pageReqVO);

    /**
     * 更新玩家余额
     *
     * @param username
     * @param changeMoney
     */
    void opsBalance(String username, BigDecimal changeMoney, String currency);

    VendorUserDO makeUser(String currency, GameVendorDO gameVendor, TenantDO tenant, @NotEmpty String username, String vendorUsername);

    VendorUserDO getUser(@NotEmpty String username);
    VendorUserDO getUser(@NotEmpty String username,@NotEmpty String vendorCode);

    VendorUserDO getUserByVendor(String username);

    void asyncBalance(String username, BigDecimal amount, String currency);

    VendorUserFlowDO depositAmount(String refId, String username, BigDecimal changeAmount, String currency);

    VendorUserFlowDO withdrawAmount(String refId, String username, BigDecimal abs, String currency);

    VendorUserBalanceDO getBalance(String username, String currency);

    VendorUserDO getUserByUsername(String username);

    VendorUserDO getUserByVendor(String username, String vendorCode);
}