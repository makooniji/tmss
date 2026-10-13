package cn.iocoder.yudao.report.mq;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mq.kafka.KafkaTopicConfig;
import cn.iocoder.yudao.module.system.model.dto.GameStaticDTO;
import cn.iocoder.yudao.report.excute.TenantStatExecutor;
import cn.iocoder.yudao.report.mapper.SystemMqErrorLogMapper;
import cn.iocoder.yudao.report.model.SystemMqErrorLogDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;


@ConditionalOnProperty(value = "spring.kafka.enable", havingValue = "true")
@Component
@Slf4j
@RequiredArgsConstructor
public class GameStatConsumer {

    private final SystemMqErrorLogMapper mqErrorLogMapper;
    private static final String GROUP_ID = "stat-group";
    private final TenantStatExecutor executor;

    private final KafkaTopicConfig kafkaTopicConfig;

    @KafkaListener(topics = "#{@kafkaTopicConfig.getStatTopic()}", groupId = GROUP_ID, containerFactory = "universalKafkaListenerContainerFactory")
    public void onMessage(GameStaticDTO message,
                          Acknowledgment ack) {

        log.info("[Kafka消费],Data:{}",JsonUtils.toJsonString(message));


        try {
            log.info("[Kafka消费][租户ID={}],Data:{}", message.getTenantId(),JsonUtils.toJsonString(message));

            // 调用抽离出的事务执行层
            executor.executeInTransaction(message.getTenantId(), List.of(message));


        } catch (Exception e) {
            log.error("[局部失败] 租户ID={} 处理异常，准备记入死信库", message.getTenantId(), e);
            saveToErrorLog(kafkaTopicConfig.getStatTopic(), message.getTenantId(), List.of(message), e);
        }finally {
            ack.acknowledge();
        }

    }

    /**
     * 持久化失败数据到数据库表
     */
    private void saveToErrorLog(String topic, Long tenantId, List<GameStaticDTO> messages, Exception e) {
        try {
            SystemMqErrorLogDO model = new SystemMqErrorLogDO();
            model.setTopic(topic);
            model.setGroupId(GROUP_ID);
            model.setTenantId(tenantId);
            model.setMessagePayload(JsonUtils.toJsonString(messages));
            model.setExceptionMsg(ExceptionUtil.stacktraceToString(e));
            model.setCreateTime(LocalDateTime.now());
            mqErrorLogMapper.insert(model);
        } catch (Exception ex) {
            log.error("!!! 死信记录写入数据库失败 !!!", ex);
        }
    }
}