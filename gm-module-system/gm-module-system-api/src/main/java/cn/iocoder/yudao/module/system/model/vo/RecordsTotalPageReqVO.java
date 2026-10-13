package cn.iocoder.yudao.module.system.model.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.util.date.TimeZoneUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 用户游戏记录分页 Request VO")
@Data
public class RecordsTotalPageReqVO extends PageParam {

   /**
    * 开始时间
    */
   private String days;
   /**
    * 结束时间
    */
   private String endDays;

   /**
    * 客户端时区，如 Asia/Shanghai；传入后 days/endDays 将自动转换为数据库时区
    */
   private String zone;

   /**
    * 商户编码
    */
   private String tenantCode;
   /**
    * 场馆编码
    */
   private String vendorCode;

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