package cn.iocoder.yudao.framework.mybatis.method;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlInjectionUtils;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import java.util.List;
import java.util.function.Predicate;

public class InsertIgnoreBatch extends AbstractMethod {

    private Predicate<TableFieldInfo> predicate;

    public InsertIgnoreBatch() {
        super("insertIgnoreBatch");
    }

    public InsertIgnoreBatch(Predicate<TableFieldInfo> predicate) {
        super("insertIgnoreBatch");
        this.predicate = predicate;
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass,
                                                 Class<?> modelClass,
                                                 TableInfo tableInfo) {

        KeyGenerator keyGenerator = NoKeyGenerator.INSTANCE;

        List<TableFieldInfo> fieldList = tableInfo.getFieldList();

        // 拼接字段
        String insertSqlColumn = tableInfo.getKeyInsertSqlColumn(true, null, false)
                + this.filterTableFieldInfo(fieldList, predicate,
                TableFieldInfo::getInsertSqlColumn, "");

        String columnScript = "(" + insertSqlColumn.substring(0, insertSqlColumn.length() - 1) + ")";

        // 拼接值
        String insertSqlProperty = tableInfo.getKeyInsertSqlProperty(true, "et.", false)
                + this.filterTableFieldInfo(fieldList, predicate,
                i -> i.getInsertSqlProperty("et."), "");

        insertSqlProperty = "(" + insertSqlProperty.substring(0, insertSqlProperty.length() - 1) + ")";

        String valuesScript = SqlScriptUtils.convertForeach(
                insertSqlProperty,
                "list",
                null,
                "et",
                ","
        );

        // 主键处理
        String keyProperty = null;
        String keyColumn = null;

        if (tableInfo.havePK()) {
            if (tableInfo.getIdType() == IdType.AUTO) {
                keyGenerator = Jdbc3KeyGenerator.INSTANCE;
                keyProperty = tableInfo.getKeyProperty();
                keyColumn = SqlInjectionUtils.removeEscapeCharacter(tableInfo.getKeyColumn());
            } else if (tableInfo.getKeySequence() != null) {
                keyGenerator = TableInfoHelper.genKeyGenerator(this.methodName, tableInfo, this.builderAssistant);
                keyProperty = tableInfo.getKeyProperty();
                keyColumn = tableInfo.getKeyColumn();
            }
        }

        // ⭐ 核心：INSERT IGNORE
        String sql = "INSERT IGNORE INTO %s %s VALUES %s";

        sql = String.format(sql, tableInfo.getTableName(), columnScript, valuesScript);

        SqlSource sqlSource = this.createSqlSource(configuration, sql, modelClass);

        return this.addInsertMappedStatement(
                mapperClass,
                modelClass,
                methodName,
                sqlSource,
                keyGenerator,
                keyProperty,
                keyColumn
        );
    }
}