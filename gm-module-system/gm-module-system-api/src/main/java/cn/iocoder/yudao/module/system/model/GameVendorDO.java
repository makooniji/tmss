package cn.iocoder.yudao.module.system.model;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.framework.mybatis.core.type.StringListTypeHandler;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Fastjson2TypeHandler;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 上游游戏厂商 DO
 *
 * @author osca
 */
@TableName(value = "game_vendor", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TenantIgnore
public class GameVendorDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
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
     * 实现厂商
     */
    private String adapter;
    /**
     * LOGO
     */
    private String logo;
    /**
     * 货币
     *
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> currency;

    /**
     * 大类code，厂商所属游戏类型，多个用逗号分隔
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> categoryCode;

    /**
     * 支持语言
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> supportLang;

    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> ipAddress;

    /**
     * 状态
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

    private String vendorChannel;

    private String vendorAgent;

    private String apiUrl;

    private String reportUrl;

    private String vendorKey;

    @Data
    public  static  class  SubVendor{
        private String apiUrl;
        private String vendorAgent;
        private String vendorKey;
        private String currency;
        private String account;
    }

    /**
     * 子商户
     */
    @TableField(typeHandler = FastjsonTypeHandler.class)
    private List<SubVendor> subVendors;



}
