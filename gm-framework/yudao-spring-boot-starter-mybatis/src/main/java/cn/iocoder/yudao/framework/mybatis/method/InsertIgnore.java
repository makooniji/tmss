package cn.iocoder.yudao.framework.mybatis.method;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;

/**
 * INSERT IGNORE 方法
 *
 * @author osca
 */
public class InsertIgnore extends AbstractMethod {

    public static final String METHOD_NAME = "insertIgnore";

    public static final String SQL_TEMPLATE =
            "<script>\n" +
            "    INSERT IGNORE INTO %s %s VALUES %s\n" +
            "</script>";

    public InsertIgnore() {
        super(METHOD_NAME);
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass,
                                                 Class<?> modelClass,
                                                 TableInfo tableInfo) {

        KeyGenerator keyGenerator = NoKeyGenerator.INSTANCE;

        // 拼接字段
        String columnScript = SqlScriptUtils.convertTrim(
                tableInfo.getAllInsertSqlColumnMaybeIf(null),
                StringPool.LEFT_BRACKET,
                StringPool.RIGHT_BRACKET,
                null,
                StringPool.COMMA
        );

        // 拼接值
        String valuesScript = SqlScriptUtils.convertTrim(
                tableInfo.getAllInsertSqlPropertyMaybeIf(null),
                StringPool.LEFT_BRACKET,
                StringPool.RIGHT_BRACKET,
                null,
                StringPool.COMMA
        );

        String keyProperty = null;
        String keyColumn = null;

        // 主键处理
        if (StringUtils.isNotBlank(tableInfo.getKeyProperty())) {

            if (tableInfo.getIdType() == IdType.AUTO) {
                // 自增主键
                keyGenerator = Jdbc3KeyGenerator.INSTANCE;
                keyProperty = tableInfo.getKeyProperty();
                keyColumn = tableInfo.getKeyColumn();

            } else if (tableInfo.getKeySequence() != null) {
                keyGenerator = TableInfoHelper.genKeyGenerator(
                        METHOD_NAME,
                        tableInfo,
                        this.builderAssistant
                );
                keyProperty = tableInfo.getKeyProperty();
                keyColumn = tableInfo.getKeyColumn();
            }
        }

        // 生成 SQL
        String sql = String.format(
                SQL_TEMPLATE,
                tableInfo.getTableName(),
                columnScript,
                valuesScript
        );

        // 构建 SQLSource
        return this.addInsertMappedStatement(
                mapperClass,
                modelClass,
                METHOD_NAME,
                this.languageDriver.createSqlSource(configuration, sql, modelClass),
                keyGenerator,
                keyProperty,
                keyColumn
        );
    }
}