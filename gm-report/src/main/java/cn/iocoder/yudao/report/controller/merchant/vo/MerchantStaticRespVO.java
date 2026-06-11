package cn.iocoder.yudao.report.controller.merchant.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;


@Data
@ExcelIgnoreUnannotated
public class MerchantStaticRespVO {

    private Long id;

    @Schema(description = "时间维度")
    @ExcelProperty("时间维度")
    private String days;

    private Long tenantId;

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


    /**
     * 输赢金额
     */
    @ExcelProperty("输赢金额")
    private BigDecimal winLoss;

    /**
     * 代理ID
     */
    private String agentCode;
    /**
     * 充值金额
     */
    private BigDecimal rechargeAmount;
    /**
     * 提现金额
     */
    private BigDecimal withdrawAmount;
    /**
     * 充值次数
     */
    private Long rechargeCount;
    /**
     * 提现次数
     */
    private Long withdrawCount;
    /**
     * 活动奖金
     */
    private BigDecimal bonus;


    /**
     * 会员ID
     */
    private Long userId;
    /**
     * 会员账号
     */
    private String username;
    /**
     * 新增会员数量
     */
    private Long newUserCount;
    /**
     * 活跃会员数据
     */
    private Long activeUserCount;
    /**
     * 有效会员数量
     */
    private Long validUserCount;



}
