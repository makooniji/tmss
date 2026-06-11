package cn.iocoder.yudao.report.controller.game.vo;


import cn.iocoder.yudao.framework.common.util.date.TimeZoneUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 统计查询 request VO
 */
@Data
public class TenantChartsReqVO {

    /**
     * 开始时间
     */
    private String days;
    /**
     * 结束时间
     */
    private String endDays;

    /**
     * 租户编码
     */

    private String tenantCode;
    /**
     * 租户名称
     */
    private String tenantName;

    /**
     * 厂商编码
     */
    private String vendorCode;

    /**
     * 厂商名称
     */
    private String vendorName;
    /**
     * 游戏编码
     */
    private String gameCode;
    /**
     * 游戏名称
     */
    private String gameName;

    /**
     * 状态
     */
    private Integer status;


    /**
     * payAmount,betAmount,winLoss,betCount,rtp,userBetCountUserCount,betCountUserBetCount,userCount
     */
    private String orderBy;

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