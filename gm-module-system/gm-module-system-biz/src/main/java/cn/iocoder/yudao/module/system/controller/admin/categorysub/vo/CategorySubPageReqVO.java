package cn.iocoder.yudao.module.system.controller.admin.categorysub.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 游戏分类分页 Request VO")
@Data
public class CategorySubPageReqVO extends PageParam {

    @Schema(description = "分类名称")
    private String title;

    @Schema(description = "分类编码")
    private String code;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "语言配置")
    private String lang;

    @Schema(description = "大类名称", example = "李四")
    private String cateName;

    @Schema(description = "大类ID", example = "20016")
    private Long cateId;

}