package cn.iocoder.yudao.report.controller.game.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;

import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 租户游戏日报 Response VO")
@Data
@ExcelIgnoreUnannotated
public class TenantDayStaticRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "13275")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "时间维度")
    @ExcelProperty("时间维度")
    private String days;

    private Long tenantId;
    @Schema(description = "租户编码")
    @ExcelProperty("租户编码")
    private String tenantCode;

    @Schema(description = "租户名称", example = "王五")
    @ExcelProperty("租户名称")
    private String tenantName;

    @Schema(description = "厂商编码")
    @ExcelProperty("厂商编码")
    private String vendorCode;

    @Schema(description = "厂商名称", example = "李四")
    @ExcelProperty("厂商名称")
    private String vendorName;


    @Schema(description = "游戏编码")
    @ExcelProperty("游戏编码")
    private String gameCode;

    @Schema(description = "游戏名称", example = "王五")
    @ExcelProperty("游戏名称")
    private String gameName;

    @Schema(description = "游戏分类 ID")
    @JsonProperty("cate_id")
    private Long cateId;

    @ExcelProperty("游戏分类")
    private String cateName;

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
     * 输赢金额
     */
    @ExcelProperty("输赢金额")
    private BigDecimal winLoss;

    /**
     * 租户详情
     */
    private TenantDO tenantDO;

}
