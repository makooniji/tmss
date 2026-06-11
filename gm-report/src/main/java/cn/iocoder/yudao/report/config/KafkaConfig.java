package cn.iocoder.yudao.report.config;

import cn.iocoder.yudao.framework.mq.kafka.StaticDTO;
import cn.iocoder.yudao.framework.mq.kafka.merchant.MerchantStaticDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    /**
     * 1. 默认工厂（消费 StaticDTO）
     * 加上 @Primary 确保默认的 @KafkaListener 在未指定 ContainerFactory 时优先使用它
     */
    @Bean
    @Primary
    public ConcurrentKafkaListenerContainerFactory<String, StaticDTO> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, StaticDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        // 🔥 核心：基于全局配置，克隆并定制专属的 ConsumerFactory
        Map<String, Object> props = new HashMap<>(consumerFactory.getConfigurationProperties());
        props.put("spring.json.use.type.headers", "false"); // 忽略上游传递的 Type Headers
        props.put("spring.json.value.default.type", StaticDTO.class.getName()); // 强行指定转为 StaticDTO

        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(props));
        factory.setBatchListener(true);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(5000L, 2)));
        return factory;
    }

    /**
     * 2. 商户统计专属工厂（消费 MerchantStaticDTO）
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MerchantStaticDTO> merchantKafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, MerchantStaticDTO> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        // 🔥 核心：基于全局配置，克隆并定制专属的 ConsumerFactory
        Map<String, Object> props = new HashMap<>(consumerFactory.getConfigurationProperties());
        props.put("spring.json.use.type.headers", "false"); // 忽略上游传递的 Type Headers
        props.put("spring.json.value.default.type", MerchantStaticDTO.class.getName()); // 强行指定转为 MerchantStaticDTO

        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(props));
        factory.setBatchListener(true);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(5000L, 2)));
        return factory;
    }
}
