package cn.iocoder.yudao.module.system.model.game.response;

import cn.hutool.core.util.ObjectUtil;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.module.system.model.CategoryDO;
import cn.iocoder.yudao.module.system.model.CategorySubDO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fhs.core.trans.anno.Trans;
import com.fhs.core.trans.constant.TransType;
import com.fhs.core.trans.vo.VO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@ExcelIgnoreUnannotated
public class GameRespVO implements VO {



    @JsonIgnore
    private Long id;

    @Schema(description = "厂商编码")
    @ExcelProperty("厂商编码")
    private String vendorCode;

    @Schema(description = "游戏名称", example = "李四")
    @ExcelProperty("游戏名称")
    private String gameName;

    @Schema(description = "游戏CODE")
    @ExcelProperty("游戏CODE")
    private String gameCode;

    @Schema(description = "游戏分类", example = "5186")
    @Trans(type = TransType.SIMPLE, target = CategoryDO.class, ref = "cateName", fields = "code")
    @JsonIgnore
    private Long cateId;

    /**
     * 分类名称
     */
    @JsonIgnore
    private String cateName;

    @Schema(description = "游戏分类", example = "5186")
    @Trans(type = TransType.SIMPLE, target = CategorySubDO.class, ref = "subCateName", fields = "code")
    @JsonIgnore
    private Long subCateId;

    /**
     * 分类名称
     */
    @JsonIgnore
    private String subCateName;

    @Schema(description = "大类 code")
    public String getCategoryCode() {
        return ObjectUtil.isNotEmpty(cateName) ? cateName : "";
    }

    @Schema(description = "小类 code")
    public String getSubCategoryCode() {
        return ObjectUtil.isNotEmpty(subCateName) ? subCateName : "";
    }

    @Schema(description = "封面")
    @ExcelProperty("封面")
    private String logo;

    @Schema(description = "维护状态")
    private Integer maintain;

    @Schema(description = "状态")
    private Integer status;

    /**
     * 支持语言
     */
    private List<String> supportLang;
    /**
     * 当前货币
     */
    private List<String> supportCurrency;

    @Schema(description = "多语言名称，如 {\"zh-CN\":{\"gameName\":\"...\"},\"en\":{\"gameName\":\"...\"}}")
    private Map<String, Map<String, String>> lang;

}
