package cn.iocoder.yudao.framework.tenant.core.aop;

import cn.iocoder.yudao.framework.common.biz.system.tenant.TenantCommonApi;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.List;

@Aspect
@Slf4j
@RequiredArgsConstructor
public class TenantSubScopeAspect {

    private final TenantCommonApi tenantCommonApi;

    @Around("@annotation(tenantSubScope) || @within(tenantSubScope)")
    public Object around(ProceedingJoinPoint joinPoint, TenantSubScope tenantSubScope) throws Throwable {
        Long tenantId = TenantContextHolder.getTenantId();
        
        // 如果处于忽略租户模式或未获取到当前租户 ID，直接放行
        if (tenantId == null || TenantContextHolder.isIgnore()) {
            return joinPoint.proceed();
        }

        // 保存之前上下文中的 tenantIds（兼容嵌套调用场景）
        List<Long> oldTenantIds = TenantContextHolder.getTenantIds();
        try {
            // 查询当前租户及其下级所有代理租户的 ID 集合
            List<Long> scopedTenantIds = tenantCommonApi.getScopedTenantIds(tenantId);
            if (scopedTenantIds != null && !scopedTenantIds.isEmpty()) {
                // 设置到 TenantContextHolder 供 TenantDatabaseInterceptor 拦截使用
                TenantContextHolder.setTenantIds(scopedTenantIds);
            }
            return joinPoint.proceed();
        } finally {
            // 方法执行完后，必须还原上下文，避免线程复用导致数据污染
            TenantContextHolder.setTenantIds(oldTenantIds);
        }
    }
}