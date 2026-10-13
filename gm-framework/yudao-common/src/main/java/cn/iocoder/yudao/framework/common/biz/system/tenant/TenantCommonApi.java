package cn.iocoder.yudao.framework.common.biz.system.tenant;

import java.util.List;

/**
 * 多租户的 API 接口
 *
 * @author osca
 */
public interface TenantCommonApi {

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
    void validateTenant(Long id);

    /**
     * 获得指定租户的数据库查询范围（当前租户 + 直属代理）。
     * <p>
     * 供业务显式扩展查询范围；一般调用 {@link cn.iocoder.yudao.module.system.service.TenantService#getVisibleTenantIds()} 即可
     *（会同步写入拦截器所需的租户范围）。
     *
     * 系统租户返回 {@code null}，表示不扩展为 IN 条件；代理返回仅含自身的列表；租户返回自身及直属代理。
     *
     * @param tenantId 租户编号
     * @return 查询范围租户编号列表
     */
    List<Long> getScopedTenantIds(Long tenantId);

}
