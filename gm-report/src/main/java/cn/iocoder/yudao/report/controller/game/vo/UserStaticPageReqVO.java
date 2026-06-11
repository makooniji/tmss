package cn.iocoder.yudao.report.controller.game.vo;


import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.date.TimeZoneUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


/**
 * 会员统计查询模型
 */
@Data
public class UserStaticPageReqVO extends PageParam {

    @Schema(description = "时间维度")
    private String days;

    private String endDays;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "客户端时区，如 Asia/Shanghai；传入后 days/endDays 将自动转换为数据库时区")
    private String zone;

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

}