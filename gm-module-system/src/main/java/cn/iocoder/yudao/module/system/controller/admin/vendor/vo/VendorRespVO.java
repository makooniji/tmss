package cn.iocoder.yudao.module.system.controller.admin.vendor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;

@Schema(description = "管理后台 - 游戏厂商 Response VO")
@Data
@ExcelIgnoreUnannotated
public class VendorRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "2264")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "游戏厂商编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("游戏厂商编码")
    private String vendorCode;

    @Schema(description = "游戏厂商简称", example = "王五")
    @ExcelProperty("游戏厂商简称")
    private String vendorName;

    @Schema(description = "LOGO")
    @ExcelProperty("LOGO")
    private String logo;

    @Schema(description = "货币")
    @ExcelProperty(value = "货币", converter = DictConvert.class)
    private List<String> currency;

    @Schema(description = "状态", example = "1")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat("common_status") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer status;

    @Schema(description = "备注", example = "你猜")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
