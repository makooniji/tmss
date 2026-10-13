package cn.iocoder.yudao.module.system.model.vo;

import cn.iocoder.yudao.framework.common.util.date.TimeZoneUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 游戏场馆总记录 Request VO")
@Data
public class RecordsTotalReqVO implements Serializable  {
   @Serial
   private static final long serialVersionUID = 1L;
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
   private String days;

   @Schema(description = "结束日期")
   private String endDays;

   @Schema(description = "客户端时区，如 Asia/Shanghai；传入后 days/endDays 将自动转换为数据库时区")
   private String zone;

   @Schema(description = "订单的创建时间")
   @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
   private LocalDateTime[] createTime;

   @Schema(description = "租户ID")
   private Long tenantId;

   public LocalDateTime[] getCreateTime() {
      if (zone != null && !zone.isBlank() && days != null && endDays != null) {
         DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND);
         String startDay = normalizeDate(days);
         String endDay = normalizeDate(endDays);
         return new LocalDateTime[]{
                 LocalDateTime.parse(TimeZoneUtils.convertDayStart(startDay, zone) + " 00:00:00", formatter),
                 LocalDateTime.parse(TimeZoneUtils.convertDayEnd(endDay, zone) + " 23:59:59", formatter)
         };
      }
      return createTime;
   }

   private static String normalizeDate(String value) {
      return value.length() >= 10 ? value.substring(0, 10) : value;
   }

}
