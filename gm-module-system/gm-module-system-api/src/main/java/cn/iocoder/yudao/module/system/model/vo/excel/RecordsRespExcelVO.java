package cn.iocoder.yudao.module.system.model.vo.excel;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 用户游戏记录 Excel VO")
@Data
@ExcelIgnoreUnannotated
public class RecordsRespExcelVO {

    @Schema(description = "玩家ID")
    @ExcelProperty("玩家ID")
    private Long userId;

    @Schema(description = "玩家账号")
    @ExcelProperty("玩家账号")
    private String username;

    @Schema(description = "下注金额")
    @ExcelProperty("下注金额")
    private BigDecimal betAmount;

    @Schema(description = "赔率")
    @ExcelProperty("赔率")
    private BigDecimal odds;

    @Schema(description = "派彩金额")
    @ExcelProperty("派彩金额")
    private BigDecimal winAmount;

    @Schema(description = "赢亏")
    @ExcelProperty("赢亏")
    private BigDecimal winLoss;

    @Schema(description = "状态")
    @ExcelProperty("状态")
    private String actionName;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
