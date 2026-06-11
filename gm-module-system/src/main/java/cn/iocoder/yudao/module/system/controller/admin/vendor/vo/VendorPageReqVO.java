package cn.iocoder.yudao.module.system.controller.admin.vendor.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 游戏厂商分页 Request VO")
@Data
public class VendorPageReqVO extends PageParam {

    @Schema(description = "游戏厂商编码")
    private String vendorCode;

    @Schema(description = "游戏厂商简称", example = "王五")
    private String vendorName;

    @Schema(description = "LOGO")
    private String logo;

    @Schema(description = "货币")
    private List<String> currency;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}