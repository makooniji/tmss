package cn.iocoder.yudao.framework.tenant.core.db;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.biz.system.tenant.TenantCommonApi;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.config.TenantProperties;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.toolkit.SqlParserUtils;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 基于 MyBatis Plus 多租户的功能，实现 DB 层面的多租户的功能
 *
 * @author osca
 */
public class TenantDatabaseInterceptor implements TenantLineHandler {

    private final TenantCommonApi tenantCommonApi;

    /**
     * 忽略的表
     *
     * KEY：表名
     * VALUE：是否忽略
     */
    private final Map<String, Boolean> ignoreTables = new HashMap<>();

    public TenantDatabaseInterceptor(TenantProperties properties, TenantCommonApi tenantCommonApi) {
        this.tenantCommonApi = tenantCommonApi;
        // 不同 DB 下，大小写的习惯不同，所以需要都添加进去
        properties.getIgnoreTables().forEach(table -> addIgnoreTable(table, true));
        // 在 OracleKeyGenerator 中，生成主键时，会查询这个表，查询这个表后，会自动拼接 TENANT_ID 导致报错
        addIgnoreTable("DUAL", true);
    }

    @Override
    public Expression getTenantId() {
        return new LongValue(TenantContextHolder.getRequiredTenantId());
    }

    /**
     * 构建租户 WHERE 条件（单租户 = ，多租户 IN）
     */
    public Expression buildTableExpression(Table table) {
        if (ignoreTable(table.getName())) {
            return null;
        }
        Column column = getAliasColumn(table);
        List<Long> scopedTenantIds = getScopedTenantIdsForQuery();
        // 系统租户等场景：getScopedTenantIds 返回 null 表示不限制，勿退化为 tenant_id = 当前上下文
        if (scopedTenantIds == null) {
            return null;
        }
        if (scopedTenantIds.size() == 1) {
            return new EqualsTo(column, new LongValue(scopedTenantIds.get(0)));
        }
        ExpressionList<Expression> expressionList = new ExpressionList<>();
        for (Long id : scopedTenantIds) {
            expressionList.addExpressions(new LongValue(id));
        }
        // 须用 ParenthesedExpressionList，否则渲染为 IN 1, 2, 3（无括号）导致 JSQLParser 解析失败
        return new InExpression(column, new ParenthesedExpressionList(expressionList));
    }

    /**
     * 获得当前请求的租户查询范围，同一请求内只查询一次子代理列表
     */
    private List<Long> getScopedTenantIdsForQuery() {
        List<Long> cached = TenantContextHolder.getTenantIds();
        if (cached != null) {
            return cached;
        }
        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            return null;
        }
        List<Long> scopedTenantIds = tenantCommonApi.getScopedTenantIds(tenantId);
        if (scopedTenantIds != null && scopedTenantIds.size() > 1) {
            TenantContextHolder.setTenantIds(scopedTenantIds);
        }
        return scopedTenantIds;
    }

    private static Column getAliasColumn(Table table) {
        if (table.getAlias() != null) {
            return new Column(table.getAlias().getName() + ".tenant_id");
        }
        return new Column("tenant_id");
    }

    @Override
    public boolean ignoreTable(String tableName) {
        // 情况一，全局忽略多租户
        if (TenantContextHolder.isIgnore()) {
            return true;
        }
        // 情况二，忽略多租户的表
        tableName = SqlParserUtils.removeWrapperSymbol(tableName);
        Boolean ignore = ignoreTables.get(tableName.toLowerCase());
        if (ignore == null) {
            ignore = computeIgnoreTable(tableName);
            synchronized (ignoreTables) {
                addIgnoreTable(tableName, ignore);
            }
        }
        return ignore;
    }

    private void addIgnoreTable(String tableName, boolean ignore) {
        ignoreTables.put(tableName.toLowerCase(), ignore);
        ignoreTables.put(tableName.toUpperCase(), ignore);
    }

    private boolean computeIgnoreTable(String tableName) {
        if (Objects.equals(SecurityFrameworkUtils.getLoginUserId(), 1L)) {
            return true;
        }
        // 找不到的表，说明不是 yudao 项目里的，不进行拦截（忽略租户）
        TableInfo tableInfo = TableInfoHelper.getTableInfo(tableName);
        if (tableInfo == null) {
            return true;
        }
        // 如果继承了 TenantBaseDO 基类，显然不忽略租户
        if (TenantBaseDO.class.isAssignableFrom(tableInfo.getEntityType())) {
            return false;
        }
        // 如果添加了 @TenantIgnore 注解，则忽略租户
        TenantIgnore tenantIgnore = tableInfo.getEntityType().getAnnotation(TenantIgnore.class);
        return tenantIgnore != null;
    }

}
