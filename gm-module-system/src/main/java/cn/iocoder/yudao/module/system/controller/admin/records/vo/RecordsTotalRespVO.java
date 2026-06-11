package cn.iocoder.yudao.module.system.controller.admin.records.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 用户游戏记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class RecordsTotalRespVO {
    /**
     * 商户ID
     */
    @ExcelProperty("商户ID")
    private Long tenantId;
    /**
     * 商户编码
     */
    @ExcelProperty("商户编码")
    private String tenantCode;
    /**
     * 商户名称
     */
    @ExcelProperty("商户名称")
    private String tenantName;

    /**
     * 厂商名称
     */
    @ExcelProperty("厂商名称")
    private String vendorName;
    /**
     * 厂商编码
     */
    @ExcelProperty("厂商编码")
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
    @ExcelProperty("输赢金额")
    private BigDecimal winLoss;
    /**
     * RTP
     */
    @ExcelProperty("RTP")
    private BigDecimal rtp;
    /**
     * 下注次数
     */
    @ExcelProperty("下注次数")
    private Long betCount;

}
