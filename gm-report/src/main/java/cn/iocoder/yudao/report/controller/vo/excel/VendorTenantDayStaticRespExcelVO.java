package cn.iocoder.yudao.report.controller.vo.excel;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 厂商商户报表 Excel VO")
@Data
@ExcelIgnoreUnannotated
public class VendorTenantDayStaticRespExcelVO {


    @Schema(description = "商户编码")
    @ExcelProperty("商户编码")
    private String tenantCode;

    @Schema(description = "商户名称")
    @ExcelProperty("商户名称")
    private String tenantName;

    @Schema(description = "投注次数")
    @ExcelProperty("投注次数")
    private Integer betCount;

    @Schema(description = "投注金额")
    @ExcelProperty("投注金额")
    private BigDecimal betAmount;

    @Schema(description = "派彩金额")
    @ExcelProperty("派彩金额")
    private BigDecimal payAmount;

    @Schema(description = "投注人数")
    @ExcelProperty("投注人数")
    private Integer userBetCount;

    @Schema(description = "盈利人数")
    @ExcelProperty("盈利人数")
    private Integer userWinCount;

    @Schema(description = "参与人数")
    @ExcelProperty("参与人数")
    private Integer userCount;

    @Schema(description = "输赢金额")
    @ExcelProperty("输赢金额")
    private BigDecimal winLoss;

    @Schema(description = "中奖概率")
    @ExcelProperty("中奖概率")
    private BigDecimal rtp;

}
