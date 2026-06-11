package cn.iocoder.yudao.module.system.model.api.response;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;


@Data
@ExcelIgnoreUnannotated
public class VendorRespVO {


    @Schema(description = "游戏厂商编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("游戏厂商编码")
    private String vendorCode;

    @Schema(description = "游戏厂商简称", example = "李四")
    @ExcelProperty("游戏厂商简称")
    private String vendorName;

    @Schema(description = "语言")
    @ExcelProperty("语言")
    private List<String> supportLang;
    /**
     * 当前货币
     */
    @Schema(description = "货币类型")
    @ExcelProperty("货币类型")
    private List<String> supportCurrency;

}
