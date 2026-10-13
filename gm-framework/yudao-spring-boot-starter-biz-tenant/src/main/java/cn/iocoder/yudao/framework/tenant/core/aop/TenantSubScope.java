package cn.iocoder.yudao.framework.tenant.core.aop;

import java.lang.annotation.*;

/**
 * 标记 Controller 或 Service 方法包含当前租户及其所有下级代理/子租户的数据查询范围
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantSubScope {
}