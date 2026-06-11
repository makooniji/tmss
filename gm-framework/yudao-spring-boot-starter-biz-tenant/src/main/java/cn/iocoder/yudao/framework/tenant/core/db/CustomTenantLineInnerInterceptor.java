package cn.iocoder.yudao.framework.tenant.core.db;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;

/**
 * 自定义租户拦截器：将 SQL 租户条件委托给 {@link TenantDatabaseInterceptor} 构建（支持直属代理 IN）
 */
public class CustomTenantLineInnerInterceptor extends TenantLineInnerInterceptor {

    public CustomTenantLineInnerInterceptor(TenantLineHandler tenantLineHandler) {
        super(tenantLineHandler);
    }

    @Override
    public Expression buildTableExpression(Table table, Expression where, String whereSegment) {
        return ((TenantDatabaseInterceptor) getTenantLineHandler()).buildTableExpression(table);
    }

}
