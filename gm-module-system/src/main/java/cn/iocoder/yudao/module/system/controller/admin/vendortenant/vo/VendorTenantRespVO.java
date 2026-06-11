package cn.iocoder.yudao.module.system.controller.admin.vendortenant.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;

@Schema(description = "管理后台 - 租户厂商 Response VO")
@Data
@ExcelIgnoreUnannotated
public class VendorTenantRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "10815")
    @ExcelProperty("主键")
    private Long id;
    /**
     * 商户ID
     */
    @ExcelProperty("商户ID")
    private Long tenantId;
    /**
     * 商户编码
     */
    @ExcelProperty("商户编码")
    private String tenantCode;
    /**
     * 商户名称
     */
    @ExcelProperty("商户名称")
    private String tenantName;

    @Schema(description = "游戏厂商编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("游戏厂商编码")
    private String vendorCode;

    @Schema(description = "游戏厂商简称", example = "李四")
    @ExcelProperty("游戏厂商简称")
    private String vendorName;

    @Schema(description = "图标")
    @ExcelProperty("图标")
    private String logo;

    @Schema(description = "货币类型")
    @ExcelProperty("货币类型")
    private String currency;

    @Schema(description = "语言")
    @ExcelProperty("语言")
    private String supportLang;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat("common_status") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer status;

    @Schema(description = "备注", example = "随便")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "RTP")
    @ExcelProperty("RTP")
    private BigDecimal rtp;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "回调地址")
    @ExcelProperty("回调地址")
    private LocalDateTime callbackUrl;


}
