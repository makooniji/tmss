package cn.iocoder.yudao.report.controller.vo;


import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.date.TimeZoneUtils;
import cn.iocoder.yudao.framework.web.zone.ZoneType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 租户游戏日报分页 Request VO")
@Data
public class TenantDayStaticPageReqVO extends PageParam {

    @Schema(description = "时间维度")
    @ZoneType
    private LocalDateTime days;
    @ZoneType
    private LocalDateTime endDays;

    @Schema(description = "客户端时区，如 Asia/Shanghai；传入后 days/endDays 将自动转换为数据库时区")
    private String zone;

    @Schema(description = "商户 ID")
    private Long tenantId;

    @Schema(description = "租户编码")
    private String tenantCode;

    @Schema(description = "租户名称", example = "王五")
    private String tenantName;

    @Schema(description = "厂商编码")
    private String vendorCode;

    @Schema(description = "厂商名称", example = "李四")
    private String vendorName;

    private String gameCode;
    private String gameName;
    /**
     * 商户状态
     */
    private Integer status;

    private Integer limit;
    /**
     * time,betAmount
     */
    private String orderBy;

    @Getter
    @AllArgsConstructor
    public enum SortEnum {

        time,
        betAmount;
    }
}