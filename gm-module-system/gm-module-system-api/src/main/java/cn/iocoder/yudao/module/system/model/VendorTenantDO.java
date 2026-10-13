package cn.iocoder.yudao.module.system.model;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.StringListTypeHandler;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * 租户厂商 DO
 *
 * @author osca
 */
@TableName(value = "game_vendor_tenant", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class VendorTenantDO extends BaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 游戏厂商编码
     */
    private String vendorCode;
    /**
     * 游戏厂商简称
     */
    private String vendorName;

    /**
     * 货币类型
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> currency;

    /**
     * 支持语言
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> supportLang;

    private Double currencyRate;
    /**
     * 语言
     */
    private String lang;

    private Integer status;
    /**
     * 备注
     */
    private String remark;


    private Long tenantId;
    private String tenantCode;
    private String tenantName;

    private BigDecimal rtp;
}
