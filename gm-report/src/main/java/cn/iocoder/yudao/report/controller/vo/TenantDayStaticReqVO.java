package cn.iocoder.yudao.report.controller.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * 统计查询 request VO
 */
@Data
public class TenantDayStaticReqVO {

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
     * payAmount,betAmount,winLoss,betCount,rtp,userBetCount/userCount,betCount/userBetCount,userCount
     */
    private String orderBy;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "用户ID")
    private Long userId;

    private String currency;

    @Getter
    @AllArgsConstructor
    public enum StatMetricEnum {

        PAY_AMOUNT("payAmount", "派彩金额", "payAmount"),
        BET_AMOUNT("betAmount", "投注金额", "betAmount"),
        WIN_LOSS("winLoss", "盈亏", "winLoss"),
        BET_COUNT("betCount", "注单总数", "betCount"),
        OPEN_DAYS("openDays", "注单总数", "openDays"),

        // 比例类指标
        RTP("rtp", "返还率", "rtp"),
        USER_PER_BET("userBetCountUserCount", "人均注单", "userBetCountUserCount"),
        BET_PER_USER_BET("betCountUserBetCount", "单均注单", "betCountUserBetCount"), // 这里的逻辑视业务而定
        USER_COUNT("userCount", "投注人数", "userCount");

        private final String code;
        private final String description;
        private final String formula; // 可选：用于 SpEL 或前端解析的公式

        /**
         * 根据 code 获取枚举
         */
        public static StatMetricEnum fromCode(String code) {
            for (StatMetricEnum metric : values()) {
                if (metric.getCode().equalsIgnoreCase(code)) {
                    return metric;
                }
            }
            return StatMetricEnum.USER_COUNT;
        }
    }
}