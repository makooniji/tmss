package cn.iocoder.yudao.report.model;

import cn.iocoder.yudao.framework.mq.enums.MessageTypeEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.ibatis.type.EnumTypeHandler;

import java.time.LocalDateTime;

/**
 * 消息流水存储实体（对应数据库中的消息流水表）
 */
@Data
@TableName(value = "system_message_flow", autoResultMap = true)
public class SystemMessageFlowDo {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    /**
     * 关联的代理/商户编码
     */
    private String agentCode;

    /**
     * 触发本次流水的消息类型
     */
    @TableField(typeHandler = EnumTypeHandler.class)
    private MessageTypeEnum messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 创建时间（记录流水发生时间）
     */
    private LocalDateTime createTime;

    private Long tenantId;


}