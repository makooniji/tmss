package cn.iocoder.yudao.framework.mq.kafka;


import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicConfig kafkaTopicConfig;

    public void send(StaticDTO stat) {
        if (stat == null || stat.getId() == null) {
            log.warn("【Kafka发送拦截】拒绝发送空消息或缺失ID的消息");
            return;
        }

        String key = stat.getId().toString();

        // 1. 创建标准的 ProducerRecord，投递到统一的中央“交换机” Topic
        ProducerRecord<String, Object> record = new ProducerRecord<>(
                kafkaTopicConfig.getUniversalExchangeTopic(),
                key,
                stat
        );

        // 2. 注入路由键（Routing Key）Header，为未来可能引入的 Kafka Streams 预留清洗标识
        record.headers().add("routing_key", "tenant.static.update".getBytes(StandardCharsets.UTF_8));

        // 3. 异步非阻塞发送，性能拉满
        kafkaTemplate.send(record).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("【Kafka发送失败】统一交换机投递异常, 商家ID: {}, 原因: {}", key, ex.getMessage());
                // 生产环境建议：此处可挂载本地数据库持久化（如错误消息补偿表）
            }
        });

    }
}