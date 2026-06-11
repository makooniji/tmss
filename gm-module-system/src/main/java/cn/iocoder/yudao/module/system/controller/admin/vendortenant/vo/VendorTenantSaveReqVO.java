package cn.iocoder.yudao.module.system.controller.admin.vendortenant.vo;

import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 租户厂商新增/修改 Request VO")
@Data
public class VendorTenantSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "10815")
    private Long id;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "状态不能为空")
    private Integer status;


    @Schema(description = "货币类型")
    private List<String> currency;

    @Schema(description = "语言")
    private List<String> supportLang;


    @Schema(description = "备注")
    private BigDecimal remark;

    @Schema(description = "回调地址")
    private BigDecimal callbackUrl;

    @Schema(description = "RTP")
    private BigDecimal rtp;

}