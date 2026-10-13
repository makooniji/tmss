package cn.iocoder.yudao.report.controller.vo;

import cn.hutool.core.util.NumberUtil;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;

import cn.iocoder.yudao.framework.web.coinType.CoinType;
import cn.iocoder.yudao.module.system.model.tenant.TenantDO;
import com.fhs.core.trans.anno.Trans;
import com.fhs.core.trans.constant.TransType;
import com.fhs.core.trans.vo.TransPojo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Schema(description = "管理后台 - 租户游戏日报 Response VO")
@Data
@ExcelIgnoreUnannotated
public class TenantDayStaticRespVO implements TransPojo {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "13275")
//    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "时间维度")
//    @ExcelProperty("时间维度")
    private String days;

    private Long tenantId;
    @Schema(description = "商户编码")
    @ExcelProperty("商户编码")
    private String tenantCode;

    @Schema(description = "商户名称", example = "王五")
    @ExcelProperty("商户名称")
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
    @Trans(type = TransType.DICTIONARY,key = "cate-gory",ref = "cateName")
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
    @ExcelProperty("RTP")
    private BigDecimal rtp;

    public BigDecimal getRtp() {
       if(NumberUtil.equals(betAmount, BigDecimal.ZERO)){
           return BigDecimal.ZERO;
       }
       return NumberUtil.div(payAmount, betAmount,4);
    }

    @Schema(description = "投注人数", example = "28081")
    private Integer userBetCount;

    @Schema(description = "盈利人数", example = "7289")
    private Integer userWinCount;

    @Schema(description = "参与人数", example = "3555")
    @ExcelProperty("活跃玩家数")
    private Integer userCount;

    /**
     * 输赢金额
     */
    @ExcelProperty("输赢金额")
    @CoinType
    private BigDecimal winLoss;

    /**
     * 租户详情
     */
    private TenantDO tenantDO;

    @ExcelProperty("人均投注")
    private BigDecimal avgBetAmount;

    public BigDecimal getAvgBetAmount() {
        if (userCount == null || userCount == 0) {
            return BigDecimal.ZERO;
        }
        return betAmount.divide(BigDecimal.valueOf(userCount), 2, RoundingMode.HALF_UP);
    }

    @ExcelProperty("人均派彩")
    private BigDecimal avgPayAmount;

    public BigDecimal getAvgPayAmount() {
        if (userCount == null || userCount == 0) {
            return BigDecimal.ZERO;
        }
        return payAmount.divide(BigDecimal.valueOf(userCount), 2, RoundingMode.HALF_UP);
    }
}
