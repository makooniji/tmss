package cn.iocoder.yudao.module.system.controller.admin.records.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 用户游戏记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class VendorTotalRespVO {

    /**
     * 厂商编码
     */
    private String vendorCode;

    /**
     * 下注金额
     */
    @ExcelProperty("下注金额")
    private BigDecimal betAmount;


    /**
     * 派彩金额
     */
    @ExcelProperty("派彩金额")
    private BigDecimal winAmount;

    /**
     * 输赢金额
     */
    private BigDecimal winLoss;
    /**
     * 人均投注金额
     */
    private BigDecimal avgBetAmount;

    /**
     * 中奖率
     */
    private BigDecimal rtp;
    /**
     * 下注次数
     */
    private Long betCount;

}
