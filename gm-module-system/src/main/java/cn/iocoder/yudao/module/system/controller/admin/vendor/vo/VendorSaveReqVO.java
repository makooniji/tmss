package cn.iocoder.yudao.module.system.controller.admin.vendor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 游戏厂商新增/修改 Request VO")
@Data
public class VendorSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "2264")
    private Long id;

    @Schema(description = "游戏厂商编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "游戏厂商编码不能为空")
    private String vendorCode;

    @Schema(description = "游戏厂商简称", example = "王五")
    private String vendorName;

    @Schema(description = "LOGO")
    private String logo;

    @Schema(description = "货币")
    private List<String> currency;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "备注", example = "你猜")
    private String remark;

}