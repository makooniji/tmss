package cn.iocoder.yudao.module.system.controller.admin.records.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 用户游戏记录新增/修改 Request VO")
@Data
public class RecordsSaveReqVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "12957")
    private Long id;

    @Schema(description = "用户ID", example = "5630")
    private Long userId;

    @Schema(description = "状态")
    private Integer action;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "三方订单号")
    private String refOrderNo;

    @Schema(description = "房间号")
    private String roomNo;

    @Schema(description = "局号期号")
    private String issueNo;

    @Schema(description = "下注金额")
    private BigDecimal betAmount;

    @Schema(description = "投注内容")
    private String betContent;

    @Schema(description = "派彩金额")
    private Long winAmount;

    @Schema(description = "拓展内容")
    private String extraContent;

    @Schema(description = "厂商编码")
    private String vendorCode;

    @Schema(description = "游戏编码")
    private String gameCode;

    @Schema(description = "游戏类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "游戏类型不能为空")
    private Integer gameKind;

    @Schema(description = "赔率信息")
    private String odds;

    @Schema(description = "备注信息", example = "随便")
    private String remark;

}