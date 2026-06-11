package cn.iocoder.yudao.report.controller.game.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 租户游戏日报 Response VO")
@Data
@ExcelIgnoreUnannotated
public class UserStaticRespVO {

    /**
     * 玩家ID
     */
    private Long userId;
    /**
     * 玩家账号
     */
   private String username;




    @Schema(description = "投注次数", example = "6030")
    @ExcelProperty("投注次数")
    private Integer betCount;

    @Schema(description = "投注金额")
    @ExcelProperty("投注金额")
    private BigDecimal betAmount;

    @Schema(description = "派彩金额")
    @ExcelProperty("派彩金额")
    private BigDecimal payAmount;

    /**
     * 输赢金额
      */
    @ExcelProperty("输赢金额")
    private BigDecimal winLoss;

}
