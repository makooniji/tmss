package cn.iocoder.yudao.module.system.controller.admin.categorysub.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.category.CategoryDO;
import com.fhs.core.trans.anno.Trans;
import com.fhs.core.trans.constant.TransType;
import com.fhs.core.trans.vo.TransPojo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "管理后台 - 游戏分类 Response VO")
@Data
@ExcelIgnoreUnannotated
public class CategorySubRespVO implements TransPojo {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1566")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "分类名称")
    @ExcelProperty("分类名称")
    private String title;

    @Schema(description = "分类编码")
    @ExcelProperty("分类编码")
    private String code;

    @Schema(description = "图片")
    @ExcelProperty("图片")
    private String logo;

    @Schema(description = "状态", example = "1")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat("common_status") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中
    private Integer status;

    @Schema(description = "排序")
    @ExcelProperty("排序")
    private Integer sort;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    /**
     * 语言配置
     */
    private Map<String, Map<String, String>> lang;

    @Schema(description = "大类名称", example = "李四")
    @ExcelProperty("大类名称")
    private String cateName;

    @Schema(description = "大类ID", example = "20016")
    @ExcelProperty("大类ID")
    @Trans(type = TransType.SIMPLE, target = CategoryDO.class, ref = "cateName", fields = "title")
    private Long cateId;

}
