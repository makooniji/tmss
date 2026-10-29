package cn.iocoder.yudao.module.system.model.vo.gameinfo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.framework.excel.core.convert.StringListConverter;
import cn.iocoder.yudao.framework.log.enums.DictTypeConstants;
import cn.iocoder.yudao.module.system.model.CategoryDO;
import cn.iocoder.yudao.module.system.model.CategorySubDO;

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
    @ExcelProperty("${InfoRespVO_id}")
    private Integer id;

    @Schema(description = "厂商编码")
    @ExcelProperty("${InfoRespVO_vendor_code}")
    private String vendorCode;

    @Schema(description = "游戏名称", example = "李四")
    @ExcelProperty("${InfoRespVO_name}")
    private String gameName;

    @Schema(description = "游戏CODE")
    @ExcelProperty("${InfoRespVO_code}")
    private String gameCode;

    @Schema(description = "游戏分类", example = "5186")

    @Trans(type = TransType.SIMPLE, target = CategoryDO.class, ref = "cateTitle", fields = "title")
    private Long cateId;

    @ExcelProperty(value = "${InfoRespVO_cate_title}")
    private String cateTitle;
    /**
     * 游戏小类
     */
    @Trans(type = TransType.SIMPLE, target = CategorySubDO.class, ref = "subCateTitle", fields = "title")
    private Long subCateId;
    @ExcelProperty(value = "${InfoRespVO_sub_cate_title}")
    private String subCateTitle;

    @Schema(description = "封面")
    @ExcelProperty("${InfoRespVO_logo}")
    private String logo;

    @Schema(description = "排序")
    @ExcelProperty("${InfoRespVO_sort}")
    private Integer sort;

    @Schema(description = "维护状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("${InfoRespVO_maintain}")
    private Integer maintain;

    @Schema(description = "创建时间")
    @ExcelProperty("${createTime}")
    private LocalDateTime createTime;

    @Schema(description = "在线人数", example = "4553")
    @ExcelProperty("${InfoRespVO_onlineCount}")
    private Integer onlineCount;

    @Schema(description = "备注", example = "你说的对")
    @ExcelProperty("${remark}")
    private String remark;
    /**
     * 状态
     */
    @ExcelProperty("${status}")
    @DictFormat(DictTypeConstants.COMMON_STATUS)
    private Integer status;


    /**
     * 支持语言
     */
    @ExcelProperty(value = "${InfoRespVO_supportLang}",converter = StringListConverter.class)
    private List<String> supportLang;
    /**
     * 当前货币
     */
    @ExcelProperty(value = "${InfoRespVO_supportCurrency}",converter = StringListConverter.class)
    private List<String> supportCurrency;

    /**
     * 语言配置
     */
    private Map<String, Map<String,String>> lang;


}
