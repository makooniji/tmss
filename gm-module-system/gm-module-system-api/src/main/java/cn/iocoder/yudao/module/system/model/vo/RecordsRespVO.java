package cn.iocoder.yudao.module.system.model.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.web.zone.ZoneType;
import cn.iocoder.yudao.module.system.model.GameRecordsDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 用户游戏记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class RecordsRespVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "12957")
    @ExcelProperty("订单ID")
    private Long id;
    /**
     * 玩家名称
     */
    @ExcelProperty("玩家账号")
    private String username;

    @ExcelProperty("玩家ID")
    private Long userId;

    @Schema(description = "状态：0=未结算 1=已结算 2=取消 3=退款")
    @ExcelProperty("状态")
    private Integer action;

    @Schema(description = "订单号")
    @ExcelProperty("订单号")
    private String orderNo;

    @Schema(description = "三方订单号")
    @ExcelProperty("三方订单号")
    private String refOrderNo;

    @Schema(description = "局号期号")
    @ExcelProperty("局号期号")
    private String issueNo;

    @Schema(description = "下注金额")
    @ExcelProperty("下注金额")
    private BigDecimal betAmount;

    @Schema(description = "投注内容")
    @ExcelProperty("投注内容")
    private String betContent;

    @Schema(description = "派彩金额")
    @ExcelProperty("派彩金额")
    private BigDecimal winAmount;
    /**
     * 输赢金额
     */
    @ExcelProperty("输赢金额")
    private BigDecimal winLoss;

    @Schema(description = "订单的创建时间")
    @ExcelProperty("创建时间")
    @ZoneType
    private LocalDateTime createTime;

    @Schema(description = "厂商编码")
    @ExcelProperty("厂商编码")
    private String vendorCode;

    @Schema(description = "游戏编码")
    @ExcelProperty("游戏编码")
    private String gameCode;

    @Schema(description = "游戏类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "游戏类型")
    private Integer gameKind;

    @Schema(description = "赔率信息")
    @ExcelProperty("赔率信息")
    private String odds;

    public static RecordsRespVO from(GameRecordsDO record) {
        if (record == null) {
            return null;
        }
        RecordsRespVO vo = BeanUtils.toBean(record, RecordsRespVO.class);
        vo.setAction(record.getAction() != null ? record.getAction().getType() : null);
        return vo;
    }

    public static List<RecordsRespVO> fromList(List<GameRecordsDO> records) {
        return records.stream().map(RecordsRespVO::from).toList();
    }

    public static PageResult<RecordsRespVO> fromPage(PageResult<GameRecordsDO> page) {
        if (page == null) {
            return null;
        }
        return new PageResult<>(fromList(page.getList()), page.getTotal(), page.getCurrentPage());
    }

}
