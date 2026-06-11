package cn.iocoder.yudao.report.controller.game.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 租户游戏日报新增/修改 Request VO")
@Data
public class TenantDayStaticSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "13275")
    private Long id;

    @Schema(description = "时间维度")
    private String days;

    @Schema(description = "租户编码")
    private String tenantCode;

    @Schema(description = "租户名称", example = "王五")
    private String tenantName;

    @Schema(description = "厂商编码")
    private String vendorCode;

    @Schema(description = "厂商名称", example = "李四")
    private String vendorName;

}