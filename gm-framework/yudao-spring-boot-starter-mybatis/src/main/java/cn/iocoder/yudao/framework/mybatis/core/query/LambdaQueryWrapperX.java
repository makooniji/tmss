package cn.iocoder.yudao.framework.mybatis.core.query;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.web.LangContextHolder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.springframework.util.StringUtils;

import java.util.Collection;

/**
 * 拓展 MyBatis Plus QueryWrapper 类，主要增加如下功能：
 * <p>
 * 1. 拼接条件的方法，增加 xxxIfPresent 方法，用于判断值不存在的时候，不要拼接到条件中。
 *
 * @param <T> 数据类型
 */
public class LambdaQueryWrapperX<T> extends LambdaQueryWrapper<T> {


    public LambdaQueryWrapperX<T> jsonContainsOrAll(SFunction<T, ?> column, String val) {
        if (StrUtil.isBlank(val)) return this;
        String columnName = columnToString(column);
        // 假设是 MySQL JSON 格式
        return (LambdaQueryWrapperX<T>) this.apply(columnName+" is null or  JSON_CONTAINS(" + columnName + ", '\"{0}\"')", val);
    }
    public LambdaQueryWrapperX<T> findContainsOrAll(SFunction<T, ?> column, String val) {
        if (StrUtil.isBlank(val)) return this;
        String columnName = columnToString(column);
        // 假设是 MySQL JSON 格式
        return (LambdaQueryWrapperX<T>) this.apply("("+columnName + " IS NULL OR FIND_IN_SET({0}, " + columnName +")"+ ")",
                val);
    }
    /**
     * 扩展：如果值存在，则进行 JSON 内部字段匹配
     * 效果：WHERE lang->'$.zh.gameName' = 'JILI'
     *
     * @param column 实体类中的 JSON 字段 (如 GameInfoDO::getLang)
     * @param field  JSON 内部的 key (如 "gameName")
     * @param val    匹配的值
     */
    public LambdaQueryWrapperX<T> eqLangPresent(SFunction<T, ?> column, String field, Object val) {
        if (ObjectUtil.isNotEmpty(val)) {
            // 1. 获取当前语言上下文
            String lang = LangContextHolder.get();
            // 2. 将 Lambda 转换为数据库列名 (如 "lang")
            String columnName = columnToString(column);

            // 3. 构造 MySQL JSON 查询语法 (根据你的数据库调整语法)
            // {0}->'$.{1}.{2}' = {3}
            String sqlExpression = String.format("%s->'$.%s.%s' = {0}", columnName, lang, field);

            return (LambdaQueryWrapperX<T>) super.apply(sqlExpression, val);
        }
        return this;
    }
    /**
     * 扩展：JSON 内部字段模糊查询
     * 效果：WHERE lang->>'$.zh.gameName' LIKE '%JILI%'
     */
    public LambdaQueryWrapperX<T> likeLangPresent(SFunction<T, ?> column, String field, Object val) {
        if (ObjectUtil.isNotEmpty(val)) {
            String lang = LangContextHolder.get();
            String columnName = columnToString(column);

            // 使用 ->> 运算符获取无引号的字符串值，这样才能进行 LIKE 匹配
            // 语法：column->>'$.lang.field' LIKE %val%
            String sqlExpression = String.format("%s->>'$.%s.%s' LIKE {0}", columnName, lang, field);

            // 自动加上百分号
            return (LambdaQueryWrapperX<T>) super.apply(sqlExpression, "%" + val + "%");
        }
        return this;
    }

    public LambdaQueryWrapperX<T> likeIfPresent(SFunction<T, ?> column, String val) {
        if (StringUtils.hasText(val)) {
            return (LambdaQueryWrapperX<T>) super.like(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> inIfPresent(SFunction<T, ?> column, Collection<?> values) {
        if (ObjectUtil.isAllNotEmpty(values) && !ArrayUtil.isEmpty(values)) {
            return (LambdaQueryWrapperX<T>) super.in(column, values);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> inIfPresent(SFunction<T, ?> column, Object... values) {
        if (ObjectUtil.isAllNotEmpty(values) && !ArrayUtil.isEmpty(values)) {
            return (LambdaQueryWrapperX<T>) super.in(column, values);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> eqIfPresent(SFunction<T, ?> column, Object val) {
        if (ObjectUtil.isNotEmpty(val)) {
            return (LambdaQueryWrapperX<T>) super.eq(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> neIfPresent(SFunction<T, ?> column, Object val) {
        if (ObjectUtil.isNotEmpty(val)) {
            return (LambdaQueryWrapperX<T>) super.ne(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> gtIfPresent(SFunction<T, ?> column, Object val) {
        if (val != null) {
            return (LambdaQueryWrapperX<T>) super.gt(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> geIfPresent(SFunction<T, ?> column, Object val) {
        if (val != null) {
            return (LambdaQueryWrapperX<T>) super.ge(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> ltIfPresent(SFunction<T, ?> column, Object val) {
        if (val != null) {
            return (LambdaQueryWrapperX<T>) super.lt(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> leIfPresent(SFunction<T, ?> column, Object val) {
        if (val != null) {
            return (LambdaQueryWrapperX<T>) super.le(column, val);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> betweenIfPresent(SFunction<T, ?> column, Object val1, Object val2) {
        if (val1 != null && val2 != null) {
            return (LambdaQueryWrapperX<T>) super.between(column, val1, val2);
        }
        if (val1 != null) {
            return (LambdaQueryWrapperX<T>) ge(column, val1);
        }
        if (val2 != null) {
            return (LambdaQueryWrapperX<T>) le(column, val2);
        }
        return this;
    }

    public LambdaQueryWrapperX<T> betweenIfPresent(SFunction<T, ?> column, Object[] values) {
        Object val1 = ArrayUtils.get(values, 0);
        Object val2 = ArrayUtils.get(values, 1);
        return betweenIfPresent(column, val1, val2);
    }

    // ========== 重写父类方法，方便链式调用 ==========

    @Override
    public LambdaQueryWrapperX<T> eq(boolean condition, SFunction<T, ?> column, Object val) {
        super.eq(condition, column, val);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> eq(SFunction<T, ?> column, Object val) {
        super.eq(column, val);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> orderByDesc(SFunction<T, ?> column) {
        super.orderByDesc(true, column);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> last(String lastSql) {
        super.last(lastSql);
        return this;
    }

    @Override
    public LambdaQueryWrapperX<T> in(SFunction<T, ?> column, Collection<?> coll) {
        super.in(column, coll);
        return this;
    }

}
