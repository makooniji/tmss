package cn.iocoder.yudao.report.controller.game.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Schema(description = "管理后台 - 商户报表 Response VO")
@Data
@ExcelIgnoreUnannotated
public class TenantGameStaticTotalVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String gameCode;
    private String gameName;

    @Schema(description = "商户总数", example = "1")
    private Integer tenantCount;

    @Schema(description = "已运营商户总数", example = "1")
    private Integer validTenantCount;

    @Schema(description = "已停运商户总数", example = "1")
    private Integer invalidTenantCount;

    @Schema(description = "投注次数", example = "6030")
    @ExcelProperty("投注次数")
    private Integer betCount;

    @Schema(description = "投注金额")
    @ExcelProperty("投注金额")
    private BigDecimal betAmount;

    @Schema(description = "派彩金额")
    @ExcelProperty("派彩金额")
    private BigDecimal payAmount;

    @Schema(description = "中奖概率")
    @ExcelProperty("中奖概率")
    private BigDecimal rtp;

    @Schema(description = "投注人数", example = "28081")
    @ExcelProperty("投注人数")
    private Integer userBetCount;

    @Schema(description = "盈利人数", example = "7289")
    @ExcelProperty("盈利人数")
    private Integer userWinCount;

    @Schema(description = "参与人数", example = "3555")
    @ExcelProperty("参与人数")
    private Integer userCount;
    /**
     * 人均投注金额
     */
    private BigDecimal avgBetAmount;
    /**
     * 输赢金额
     */
    private BigDecimal winLoss;

}
