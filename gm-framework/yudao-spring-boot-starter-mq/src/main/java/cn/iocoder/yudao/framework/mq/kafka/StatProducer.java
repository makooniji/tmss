package cn.iocoder.yudao.framework.mq.kafka;


import lombok.RequiredArgsConstructor;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StatProducer {

    private final KafkaTemplate<String, StaticDTO> kafkaTemplate;

    public void send(StaticDTO stat) {
        kafkaTemplate.send(KafkaTopics.STAT_TOPIC, stat.getId().toString(), stat);
        kafkaTemplate.send(KafkaTopics.USER_TOPIC, stat.getId().toString(), stat);
    }
}