package cn.iocoder.yudao.module.system.controller.admin.vendoruser.vo;

import lombok.*;

import java.math.BigDecimal;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 厂商玩家分页 Request VO")
@Data
public class VendorUserPageReqVO extends PageParam {

    @Schema(description = "游戏厂商ID", example = "27678")
    private Long vendorId;

    @Schema(description = "游戏厂商编码")
    private String vendorCode;

    @Schema(description = "用户ID", example = "18007")
    private Long userId;

    @Schema(description = "用户名", example = "芋艿")
    private String username;

    @Schema(description = "外部玩家ID", example = "10200")
    private String outUserId;

    @Schema(description = "状态: 0开启 1 关闭", example = "2")
    private Integer status;


    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    /**
     * 用户余额
     */
    private BigDecimal balance;
    /**
     * 租户ID
     */
    private Long tenantId;
    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 租户名称
     */
    private String tenantName;

}