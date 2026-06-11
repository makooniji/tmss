package cn.iocoder.yudao.module.system.controller.admin.gameInfo.vo;

import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.Map;

@Schema(description = "管理后台 - 游戏信息新增/修改 Request VO")
@Data
public class InfoSaveReqVO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "25152")
    private Integer id;

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

    @Schema(description = "封面")
    private String logo;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "维护状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "维护状态不能为空")
    private Integer maintain;

    @Schema(description = "在线人数", example = "4553")
    private Integer onlineCount;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    private Integer status;

    /**
     * 支持语言
     */
    private List<String> supportLang;
    /**
     * 当前货币
     */
    private List<String> supportCurrency;


    /**
     * 语言配置
     */
    private Map<String, Map<String,String>> lang;

}