package cn.iocoder.yudao.report.controller.vo.excel;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 租户游戏日报 Excel VO")
@Data
@ExcelIgnoreUnannotated
public class TenantDayStaticRespExcelVO {

    private Long tenantId;

    @Schema(description = "商户编码")
    @ExcelProperty("商户编码")
    private String tenantCode;

    @Schema(description = "商户名称", example = "王五")
    @ExcelProperty("商户名称")
    private String tenantName;

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

    @Schema(description = "输赢金额")
    @ExcelProperty("输赢金额")
    private BigDecimal winLoss;

}
