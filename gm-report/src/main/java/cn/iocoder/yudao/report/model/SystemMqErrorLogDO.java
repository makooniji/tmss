package cn.iocoder.yudao.report.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * mq错误记录
 *
 * @author osca
 */
@TableName("system_mq_error_log")
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SystemMqErrorLogDO implements Serializable {


    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private Long id;
    private String topic;

    private String groupId;

    private Long tenantId;

    private String messagePayload;

    private String exceptionMsg;
    private LocalDateTime createTime;

}
