package cn.iocoder.yudao.module.system.controller.admin.gameInfo.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 游戏信息分页 Request VO")
@Data
public class InfoPageReqVO extends PageParam {

    @Schema(description = "厂商编码")
    private String vendorCode;

    @Schema(description = "游戏名称", example = "李四")
    private String gameName;

    @Schema(description = "游戏CODE")
    private String gameCode;

    @Schema(description = "游戏分类", example = "5186")
    private Long cateId;

    /**
     * 游戏小类
     */
    private Long subCateId;

    @Schema(description = "维护状态")
    private Integer maintain;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "traceId")
    private String traceId;

    @Schema(description = "货币")
    private String currency;

    @Schema(description = "语言")
    private String language;

    private Integer status;

}