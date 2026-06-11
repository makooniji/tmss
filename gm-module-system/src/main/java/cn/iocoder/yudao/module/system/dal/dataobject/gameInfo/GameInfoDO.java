package cn.iocoder.yudao.module.system.dal.dataobject.gameInfo;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.LangDO;
import cn.iocoder.yudao.framework.mybatis.core.type.StringListTypeHandler;
import cn.iocoder.yudao.framework.mybatis.i18n.I18nField;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * 游戏信息 DO
 *
 * @author mako
 */
@TableName(value = "game_info",autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TenantIgnore
public class GameInfoDO extends LangDO {

    /**
     * ID
     */
    @TableId
    private Integer id;
    /**
     * 厂商编码
     */
    private String vendorCode;

    private String vendorName;
    /**
     * 游戏名称
     */
    @I18nField(key = "gameName")
    private String gameName;
    /**
     * 游戏CODE
     */
    private String gameCode;
    /**
     * 游戏大类
     *
     */
    private Long cateId;
    /**
     * 游戏小类
     */
    private Long subCateId;
    /**
     * 封面
     */
    private String logo;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 维护状态
     */
    private Integer maintain;
    /**
     * 在线人数
     */
    private Integer onlineCount;
    /**
     * 备注
     */
    private String remark;

    private Integer status;


    /**
     * 支持语言
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> supportLang;
    /**
     * 当前货币
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> supportCurrency;

}
