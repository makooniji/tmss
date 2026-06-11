package cn.iocoder.yudao.module.system.controller.admin.category.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Schema(description = "管理后台 - 游戏大类新增/修改 Request VO")
@Data
public class CategorySaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "14778")
    private Long id;

    @Schema(description = "分类名称")
    private String title;

    @Schema(description = "图片")
    private String logo;

    @Schema(description = "分类编码")
    private String code;

    @Schema(description = "状态", example = "2")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;

    /**
     * 语言配置
     */
    private Map<String, Map<String, String>> lang;

}