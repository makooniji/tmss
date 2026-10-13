package cn.iocoder.yudao.module.system.model.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.date.TimeZoneUtils;
import cn.iocoder.yudao.framework.web.zone.ZoneType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 用户游戏记录分页 Request VO")
@Data
public class RecordsPageReqVO extends PageParam {

    @Schema(description = "用户ID", example = "5630")
    private Long userId;

    @Schema(description = "玩家账号")
    private String username;

    @Schema(description = "状态")
    private Integer action;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "三方订单号")
    private String refOrderNo;

    @Schema(description = "房间号")
    private String roomNo;

    @Schema(description = "局号期号")
    private String issueNo;

    @Schema(description = "厂商")
    private String vendorCode;

    @Schema(description = "游戏Code")
    private String gameCode;

    @Schema(description = "编码")
    private String tenantCode;

    @Schema(description = "游戏类型")
    private Long gameKind;

    @Schema(description = "开始日期")
    @ZoneType
    private LocalDateTime days;

    @Schema(description = "结束日期")
    @ZoneType
    private LocalDateTime endDays;

    @Schema(description = "客户端时区，如 Asia/Shanghai；传入后 days/endDays 将自动转换为数据库时区")
    private String zone;

    @Schema(description = "订单的创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "租户ID")
    private Long tenantId;



}
