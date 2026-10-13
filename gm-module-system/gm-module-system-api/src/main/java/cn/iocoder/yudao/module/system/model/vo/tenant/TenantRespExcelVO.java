package cn.iocoder.yudao.module.system.model.vo.tenant;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;

import cn.iocoder.yudao.framework.log.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 租户 Excel 导出 VO")
@Data
@ExcelIgnoreUnannotated
public class TenantRespExcelVO {


    @ExcelProperty("商户编码")
    private String code;

    @ExcelProperty("商户名称")
    private String name;

    @ExcelProperty(value = "商户状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.TENANT_STATUS)
    private Integer status;
    /**
     * 创建时间
     */
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("玩家数量")
    private Integer accountCount;


}
