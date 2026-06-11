package cn.iocoder.yudao.module.system.controller.admin.tenant.vo.tenant;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import cn.iocoder.yudao.framework.excel.core.annotations.DictFormat;
import cn.iocoder.yudao.framework.excel.core.convert.DictConvert;
import cn.iocoder.yudao.module.system.enums.DictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 租户 Response VO")
@Data
@ExcelIgnoreUnannotated
public class TenantRespVO {

    /**
     * 租户ID
     */
    @ExcelProperty("商户ID")
    private Long id;

    /**
     * 租户名称
     */
    @ExcelProperty("商户名称")
    private String name;

    /**
     * 父租户编号
     */
    @ExcelProperty("父商户ID")
    private Long parentId;

    /**
     * 租户编码
     */
    @ExcelProperty("商户编码")
    private String code;

    /**
     * 商户KEY
     */
    @ExcelProperty("商户公钥")
    private String tenantKey;
    /**
     * 商户私钥
     */
    @ExcelProperty("商户私钥")
    private String secretKey;
    /**
     * 开通的货币
     */
    @ExcelProperty("开通货币")
    private List<String> currency;

    /**
     * 商户状态
     */
    @ExcelProperty(value = "商户状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.TENANT_STATUS)
    private Integer status;

    /**
     * 账号数量
     */
    @ExcelProperty("账号数量")
    private Integer accountCount;

    /**
     * 创建时间
     */
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
    /**
     * 创建人
     */
    @ExcelProperty("创建人")
    private String creator;

    /**
     * 接入方式0 免转 1转入
     */
    @ExcelProperty("接入方式")
    private Integer implType=0;

    /**
     * 开启的场馆
     */
    @ExcelProperty("开启的场馆")
    private List<String> vendorCodes;
    /**
     * 开启的游戏
     */
    @ExcelProperty("开启的游戏")
    private List<String> gameCodes;
    /**
     * 回调地址
     */
    @ExcelProperty("回调地址")
    private String notifyUrl;

    /**
     * ip加白
     */
    @ExcelProperty("ip加白")
    private List<String> ipAddress;

    private Long packageId;


}
