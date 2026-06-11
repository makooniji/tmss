package cn.iocoder.yudao.module.system.service.tenant;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.tenant.GameReqVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.tenant.GameRespVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.tenant.TenantPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.tenant.TenantSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.module.system.service.tenant.handler.TenantInfoHandler;
import cn.iocoder.yudao.module.system.service.tenant.handler.TenantMenuHandler;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Set;

/**
 * 租户 Service 接口
 *
 * @author osca
 */
public interface TenantService {

    /**
     * 创建租户
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTenant(@Valid TenantSaveReqVO createReqVO);

    /**
     * 更新租户
     *
     * @param updateReqVO 更新信息
     */
    void updateTenant(@Valid TenantSaveReqVO updateReqVO);

    /**
     * 更新租户的角色菜单
     *
     * @param tenantId 租户编号
     * @param menuIds  菜单编号数组
     */
    void updateTenantRoleMenu(Long tenantId, Set<Long> menuIds);

    /**
     * 删除租户
     *
     * @param id 编号
     */
    void deleteTenant(Long id);

    /**
     * 批量删除租户
     *
     * @param ids 编号数组
     */
    void deleteTenantList(List<Long> ids);

    /**
     * 获得租户
     *
     * @param id 编号
     * @return 租户
     */
    TenantDO getTenant(Long id);

    /**
     * 获得租户分页
     *
     * @param pageReqVO 分页查询
     * @return 租户分页
     */
    PageResult<TenantDO> getTenantPage(TenantPageReqVO pageReqVO);

    /**
     * 获得名字对应的租户
     *
     * @param name 租户名
     * @return 租户
     */
    TenantDO getTenantByName(String name);

    /**
     * 获得域名对应的租户
     *
     * @param website 域名
     * @return 租户
     */
    TenantDO getTenantByWebsite(String website);

    /**
     * 获得使用指定套餐的租户数量
     *
     * @param packageId 租户套餐编号
     * @return 租户数量
     */
    Long getTenantCountByPackageId(Long packageId);

    /**
     * 获得使用指定套餐的租户数组
     *
     * @param packageId 租户套餐编号
     * @return 租户数组
     */
    List<TenantDO> getTenantListByPackageId(Long packageId);

    /**
     * 获得指定状态的租户列表
     *
     * @param status 状态
     * @return 租户列表
     */
    List<TenantDO> getTenantListByStatus(Integer status);

    /**
     * 获得指定租户的直属代理列表
     *
     * @param parentId 租户编号
     * @return 直属代理列表
     */
    List<TenantDO> getTenantListByParentId(Long parentId);

    /**
     * 获得当前登录租户可见的租户编号列表
     *
     * 系统租户返回 {@code null}，表示不需要租户层级过滤。
     *
     * @return 可见租户编号列表
     */
    List<Long> getVisibleTenantIds();

    /**
     * 获得指定租户的数据库查询范围（当前租户 + 直属代理）
     *
     * @param tenantId 租户编号
     * @return 查询范围租户编号列表；系统租户返回 {@code null}
     */
    List<Long> getScopedTenantIds(Long tenantId);

    /**
     * 校验目标租户是否在当前登录租户的可见范围内
     *
     * @param id 租户编号
     */
    void validateTenantVisible(Long id);

    /**
     * 解析当前登录租户在查询场景下可使用的租户范围
     *
     * @param tenantId 指定租户编号；为空时表示查询全部可见范围
     * @return 可用于 SQL IN 的租户编号列表；返回 {@code null} 表示不做租户条件限制
     */
    List<Long> resolveQueryTenantIds(Long tenantId);

    /**
     * 进行租户的信息处理逻辑
     * 其中，租户编号从 {@link TenantContextHolder} 上下文中获取
     *
     * @param handler 处理器
     */
    void handleTenantInfo(TenantInfoHandler handler);

    /**
     * 进行租户的菜单处理逻辑
     * 其中，租户编号从 {@link TenantContextHolder} 上下文中获取
     *
     * @param handler 处理器
     */
    void handleTenantMenu(TenantMenuHandler handler);

    /**
     * 获得所有租户
     *
     * @return 租户编号数组
     */
    List<Long> getTenantIdList();

    /**
     * 校验租户是否合法
     *
     * @param id 租户编号
     */
    void validTenant(Long id);

    TenantDO getTenantByKey(String name);

    void updateVendorGame(@Valid GameReqVO updateReqVO);

    GameRespVO getVendorGameConfig(Long tenantId);

}
