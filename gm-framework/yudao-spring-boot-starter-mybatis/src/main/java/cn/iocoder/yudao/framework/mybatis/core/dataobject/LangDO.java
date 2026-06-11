package cn.iocoder.yudao.framework.mybatis.core.dataobject;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fhs.core.trans.vo.TransPojo;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 多语言实体对象
 *
 * @author osca
 */
@Data
public abstract class LangDO extends BaseDO {

    /**
     * 语言
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Map<String,String>> lang;

}
