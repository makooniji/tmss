package cn.iocoder.yudao.module.system.controller.admin.records.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

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
    * 商户编码
    */
   private String tenantCode;
   /**
    * 场馆编码
    */
   private String vendorCode;


}