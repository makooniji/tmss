package cn.iocoder.yudao.report.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 厂商报表 Response VO")
@Data
public class VendorDayStaticRespVO {

    @Schema(description = "厂商编码")
    private String vendorCode;

    @Schema(description = "厂商名称")
    private String vendorName;

    @Schema(description = "投注次数")
    private Integer betCount;

    @Schema(description = "投注金额")
    private BigDecimal betAmount;

    @Schema(description = "派彩金额")
    private BigDecimal payAmount;

    @Schema(description = "中奖概率")
    private BigDecimal rtp;

    @Schema(description = "投注人数")
    private Integer userBetCount;

    @Schema(description = "盈利人数")
    private Integer userWinCount;

    @Schema(description = "参与人数")
    private Integer userCount;

    @Schema(description = "输赢金额")
    private BigDecimal winLoss;

}
