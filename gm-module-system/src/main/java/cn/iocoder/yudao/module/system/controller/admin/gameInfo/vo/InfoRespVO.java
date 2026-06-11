package cn.iocoder.yudao.module.system.controller.admin.gameInfo.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.category.CategoryDO;
import cn.iocoder.yudao.module.system.dal.dataobject.categorysub.CategorySubDO;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import com.fhs.core.trans.anno.Trans;
import com.fhs.core.trans.constant.TransType;
import com.fhs.core.trans.vo.TransPojo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 游戏信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class InfoRespVO implements TransPojo {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "25152")
    @ExcelProperty("ID")
    private Integer id;

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
    @ExcelProperty(value = "游戏分类", converter = DictConvert.class)
    @Trans(type = TransType.SIMPLE, target = CategoryDO.class, ref = "cateTitle", fields = "title")
    private Long cateId;

    private String cateTitle;
    /**
     * 游戏小类
     */
    @Trans(type = TransType.SIMPLE, target = CategorySubDO.class, ref = "subCateTitle", fields = "title")
    private Long subCateId;

    private String subCateTitle;

    @Schema(description = "封面")
    @ExcelProperty("封面")
    private String logo;

    @Schema(description = "排序")
    @ExcelProperty("排序")
    private Integer sort;

    @Schema(description = "维护状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("维护状态")
    private Integer maintain;

    @Schema(description = "创建时间")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "在线人数", example = "4553")
    @ExcelProperty("在线人数")
    private Integer onlineCount;

    @Schema(description = "备注", example = "你说的对")
    @ExcelProperty("备注")
    private String remark;
    /**
     * 状态
     */
    @ExcelProperty("状态")
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;


    /**
     * 支持语言
     */
    @ExcelProperty("语言")
    private List<String> supportLang;
    /**
     * 当前货币
     */
    @ExcelProperty("货币")
    private List<String> supportCurrency;

    /**
     * 语言配置
     */
    private Map<String, Map<String,String>> lang;


}
