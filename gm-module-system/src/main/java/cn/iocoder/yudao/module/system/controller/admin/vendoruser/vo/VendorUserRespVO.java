package cn.iocoder.yudao.module.system.controller.admin.vendoruser.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 厂商玩家 Response VO")
@Data
@ExcelIgnoreUnannotated
public class VendorUserRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "4711")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "游戏厂商ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "27678")
    @ExcelProperty("游戏厂商ID")
    private Long vendorId;

    @Schema(description = "游戏厂商编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("游戏厂商编码")
    private String vendorCode;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "18007")
    @ExcelProperty("用户ID")
    private Long userId;

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    @ExcelProperty("用户名")
    private String username;

    @Schema(description = "状态: 0开启 1 关闭", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态: 0开启 1 关闭")
    private Integer status;



    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


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
