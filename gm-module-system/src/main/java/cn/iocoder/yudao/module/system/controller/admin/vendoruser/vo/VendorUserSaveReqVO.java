package cn.iocoder.yudao.module.system.controller.admin.vendoruser.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 厂商玩家新增/修改 Request VO")
@Data
public class VendorUserSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "4711")
    private Long id;

    @Schema(description = "游戏厂商ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "27678")
    @NotNull(message = "游戏厂商ID不能为空")
    private Long vendorId;

    @Schema(description = "游戏厂商编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "游戏厂商编码不能为空")
    private String vendorCode;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "18007")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @NotEmpty(message = "用户名不能为空")
    private String username;

    @Schema(description = "外部玩家ID", example = "10200")
    private String outUserId;

    @Schema(description = "状态: 0开启 1 关闭", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "状态: 0开启 1 关闭不能为空")
    private Integer status;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "操作人")
    private String updateBy;

}