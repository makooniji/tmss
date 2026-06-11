package cn.iocoder.yudao.module.system.controller.admin.records.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "管理后台 - 游戏场馆总记录 Request VO")
@Data
public class RecordsTotalReqVO implements Serializable  {
   @Serial
   private static final long serialVersionUID = 1L;
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