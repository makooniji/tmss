package cn.iocoder.yudao.report.controller.game.vo;


import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.date.TimeZoneUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Schema(description = "管理后台 - 租户游戏日报分页 Request VO")
@Data
public class TenantDayStaticPageReqVO extends PageParam {

    @Schema(description = "时间维度")
    private String days;

    private String endDays;

    @Schema(description = "客户端时区，如 Asia/Shanghai；传入后 days/endDays 将自动转换为数据库时区")
    private String zone;

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

    public String getDays() {
        if (zone != null && !zone.isBlank() && days != null) {
            return TimeZoneUtils.convertDayStart(days, zone);
        }
        return days;
    }

    public String getEndDays() {
        if (zone != null && !zone.isBlank() && endDays != null) {
            return TimeZoneUtils.convertDayEnd(endDays, zone);
        }
        return endDays;
    }

    @Getter
    @AllArgsConstructor
    public enum SortEnum {

        time,
        betAmount;
    }
}