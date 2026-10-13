package cn.iocoder.yudao.framework.mq.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    // 动态获取带有环境后缀的 Topic
    public String getUniversalExchangeTopic() {
        return "universal-tenant-static-exchange-" + activeProfile;
    }

    public String getStatTopic() {
        return "tenant_day_stat_" + activeProfile;
    }
}