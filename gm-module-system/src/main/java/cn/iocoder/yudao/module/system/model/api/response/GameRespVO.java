package cn.iocoder.yudao.module.system.model.api.response;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.module.system.dal.dataobject.category.CategoryDO;
import cn.iocoder.yudao.module.system.dal.dataobject.categorysub.CategorySubDO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fhs.core.trans.anno.Trans;
import com.fhs.core.trans.constant.TransType;
import com.fhs.core.trans.vo.VO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

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

    private String categoryCode;

    public String getCategoryCode() {
        List<String> result=new ArrayList<>();
        if(ObjectUtil.isNotEmpty(cateName)){
            result.add(cateName);
        }
        if(ObjectUtil.isNotEmpty(subCateName)){
            result.add(subCateName);
        }
        if(ObjectUtil.isEmpty(result)){
            return "";
        }


        return CollUtil.join(result, ",");
    }

    @Schema(description = "封面")
    @ExcelProperty("封面")
    private String logo;
    /**
     * 支持语言
     */
    private List<String> supportLang;
    /**
     * 当前货币
     */
    private List<String> supportCurrency;

}
