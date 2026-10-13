package cn.iocoder.yudao.module.system.model.game.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CategoryRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "分类名称")
    private String title;

    @Schema(description = "分类编码")
    private String code;

    @Schema(description = "图片")
    private String logo;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "语言配置")
    private Map<String, Map<String, String>> lang;

    @Schema(description = "小类列表")
    private List<CategorySubRespVO> subCategory;

}
