package cn.iocoder.yudao.framework.tenant.core.db;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.biz.system.tenant.TenantCommonApi;
import cn.iocoder.yudao.framework.common.util.spring.SpringUtils;
import cn.iocoder.yudao.framework.security.core.LoginUser;
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
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 基于 MyBatis Plus 多租户的功能，实现 DB 层面的多租户的功能
 *
 * @author osca
 */
@Slf4j
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
        String tableName = table.getName();
        if (ignoreTable(tableName)) {
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
        return new InExpression(column, new ParenthesedExpressionList<>(expressionList));
    }

    /**
     * 获得当前请求的租户查询范围
     */
    private List<Long> getScopedTenantIdsForQuery() {
        // 1. 优先读取业务显式指定的范围（由 @TenantSubScope 切面或业务手动设置）
        List<Long> cached = TenantContextHolder.getTenantIds();
        if (cached != null) {
            return cached;
        }

        Long tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null) {
            return null; // 无租户上下文，不加条件
        }

        // 2. 系统租户判断（建议提供专门的轻量方法，如判断是否为超级租户/ID为1，或使用缓存判断）
        if (isSystemTenant(tenantId)) {
            return null; // 系统租户不限制租户 ID
        }

        // 3. 默认仅查询当前租户
        return List.of(tenantId);
    }

    /**
     * 判断是否为系统/超级租户（可根据实际业务逻辑简化）
     */
    private boolean isSystemTenant(Long tenantId) {
        // 示例：如果是默认系统租户 ID（如 1L 或 0L），直接放行
        return Objects.equals(tenantId, 1L);
        // 如果必须调用 API，请确保 API 内部查库时加了 @TenantIgnore 或跳过了租户拦截
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
        LoginUser loginUser = SecurityFrameworkUtils.getLoginUser();
        if (ObjectUtil.isNotEmpty( loginUser)) {
            // 情况二，超级管理员跳过租户（依赖当前登录用户，不能写入 ignoreTables 缓存）
            if (Objects.equals(loginUser.getId(), 1L)) {
                return true;
            }
            if (loginUser.getRoleIds().contains(1L)) {
                return true;
            }
        }

        // 情况三，按表静态配置判断是否忽略（可缓存）
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
        // 找不到的表，说明不是 yudao 项目里的，不进行拦截（忽略租户）
        TableInfo tableInfo = TableInfoHelper.getTableInfo(tableName);
        if (tableInfo == null) {
            return true;
        }
        // 如果继承了 TenantBaseDO 基类，显然不忽略租户
        if (cn.iocoder.yudao.framework.mybatis.core.dataobject.TenantBaseDO.class.isAssignableFrom(tableInfo.getEntityType())) {
            return false;
        }
        // 如果添加了 @TenantIgnore 注解，则忽略租户
        TenantIgnore tenantIgnore = tableInfo.getEntityType().getAnnotation(TenantIgnore.class);
        return tenantIgnore != null;
    }



}
