package cn.iocoder.yudao.module.system.controller.admin.records.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 用户游戏记录分页 Request VO")
@Data
public class RecordsPageReqVO extends PageParam {

    @Schema(description = "用户ID", example = "5630")
    private Long userId;

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

    @Schema(description = "订单的创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "租户ID")
    private Long tenantId;

}