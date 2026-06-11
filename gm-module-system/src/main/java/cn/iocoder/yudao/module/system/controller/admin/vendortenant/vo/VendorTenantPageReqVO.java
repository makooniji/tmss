package cn.iocoder.yudao.module.system.controller.admin.vendortenant.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 租户厂商分页 Request VO")
@Data
public class VendorTenantPageReqVO extends PageParam {

    /**
     * 租户编码
     */
    private String tenantCode;
    /**
     * 租户名称
     */
    private String tenantName;
    @Schema(description = "游戏厂商编码")
    private String vendorCode;

    @Schema(description = "游戏厂商简称", example = "李四")
    private String vendorName;




    @Schema(description = "状态", example = "2")
    private Integer status;


    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}