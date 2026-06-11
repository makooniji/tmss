package cn.iocoder.yudao.module.system.controller.admin.categorysub.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Schema(description = "管理后台 - 游戏分类新增/修改 Request VO")
@Data
public class CategorySubSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1566")
    private Long id;

    @Schema(description = "分类名称")
    private String title;

    @Schema(description = "分类编码")
    private String code;

    @Schema(description = "图片")
    private String logo;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;

    /**
     * 语言配置
     */
    private Map<String, Map<String, String>> lang;

    @Schema(description = "大类名称", example = "李四")
    private String cateName;

    @Schema(description = "大类ID", example = "20016")
    private Long cateId;

}