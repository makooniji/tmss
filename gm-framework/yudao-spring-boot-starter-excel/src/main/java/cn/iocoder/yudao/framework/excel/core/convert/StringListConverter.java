package cn.iocoder.yudao.framework.excel.core.convert;

import cn.idev.excel.converters.Converter;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.GlobalConfiguration;
import cn.idev.excel.metadata.data.WriteCellData;
import cn.idev.excel.metadata.property.ExcelContentProperty;

import java.util.List;

/**
 * List<String> 与 Excel 字符串相互转换器
 */
public class StringListConverter implements Converter<List<String>> {

    @Override
    public Class<?> supportJavaTypeKey() {
        return List.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    /**
     * 导出 Excel 时：List<String> -> "zh_CN, en_US"
     */
    @Override
    public WriteCellData<?> convertToExcelData(List<String> value, ExcelContentProperty contentProperty,
                                              GlobalConfiguration globalConfiguration) {
        if (value == null || value.isEmpty()) {
            return new WriteCellData<>("");
        }
        // 使用逗号分隔拼接
        return new WriteCellData<>(String.join(", ", value));
    }
}