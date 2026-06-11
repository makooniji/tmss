package cn.iocoder.yudao.report.mq;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mq.kafka.StaticDTO;
import cn.iocoder.yudao.framework.mq.kafka.merchant.MerchantStaticDTO;
import cn.iocoder.yudao.report.mapper.SystemMqErrorLogMapper;
import cn.iocoder.yudao.report.model.SystemMqErrorLogDO;
import cn.iocoder.yudao.report.pipeline.excute.MerchantStatExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.mq.kafka.KafkaTopics.MERCHANT_USER_REPORT;
import static cn.iocoder.yudao.framework.mq.kafka.KafkaTopics.STAT_TOPIC;


@ConditionalOnProperty(value = "spring.kafka.enable", havingValue = "true")
@Component
@Slf4j
@RequiredArgsConstructor
public class MerchantStatConsumer {

    private final SystemMqErrorLogMapper mqErrorLogMapper;
    private final MerchantStatExecutor statExecutor;
    private static final String GROUP_ID = "stat-group";


    @KafkaListener(topics = MERCHANT_USER_REPORT, groupId = GROUP_ID, containerFactory = "merchantKafkaListenerContainerFactory")
    public void onMessage(List<MerchantStaticDTO> messages,
                          Acknowledgment ack) {

        try {
            Map<Long, List<MerchantStaticDTO>> collect = messages.stream().collect(Collectors.groupingBy(StaticDTO::getTenantId));
            collect.forEach((tenantId, tenantMessages) -> {
                try {
                    log.info("[Kafka消费][租户ID={}][游戏message数量={}]", tenantId, tenantMessages.size());
                    // 调用抽离出的事务执行层
                    statExecutor.executeInTransaction(tenantId, tenantMessages);
                } catch (Exception e) {
                    log.error("[局部失败] 租户ID={} 处理异常，准备记入死信库", tenantId, e);
                    saveToErrorLog(STAT_TOPIC, tenantId, tenantMessages, e);
                }

            });

            // 手动提交 offset（防止丢数据🔥）
            ack.acknowledge();

        } catch (Exception e) {
            log.error("处理统计失败", e);
            // 不 ack → Kafka 会重试
        }
    }

    /**
     * 持久化失败数据到数据库表
     */
    private void saveToErrorLog(String topic, Long tenantId, List<MerchantStaticDTO> messages, Exception e) {


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