package cn.iocoder.yudao.module.system.dal.dataobject.categorysub;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.LangDO;
import cn.iocoder.yudao.framework.mybatis.i18n.I18nField;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fhs.core.trans.anno.TransDefaultSett;
import lombok.*;

/**
 * 游戏分类 DO
 *
 * @author osca
 */
@TableName(value = "game_category_sub", autoResultMap = true)
@KeySequence("game_category_sub_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TransDefaultSett(defaultFields = {"id", "title", "lang", "code"})
public class CategorySubDO extends LangDO {

    /**
     * 分类编码
     */
    @TableId
    private Long id;
    /**
     * 分类名称
     */
    @I18nField(key = "title")
    private String title;

    /**
     * 编码
     */
    private String code;

    /**
     * 图片
     */
    private String logo;
    /**
     * 状态
     *
     * 枚举 {@link TODO common_status 对应的类}
     */
    private Integer status;
    /**
     * 排序
     */
    private Integer sort;

    /**
     * 大类名称
     */
    private String cateName;
    /**
     * 大类ID
     */
    private Long cateId;


}
