package cn.iocoder.yudao.framework.mq.kafka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
@ComponentScan("cn.iocoder.yudao.framework.mq.kafka") // 确保扫描到该包
public class YudaoKafkaAutoConfiguration {

    @Bean("universalKafkaListenerContainerFactory")
    @Primary
    public ConcurrentKafkaListenerContainerFactory<String, Object> universalKafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory, ObjectMapper objectMapper) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        // =================================================================
        // 1. 清理可能干扰强类型反序列化的全局配置
        // =================================================================
        Map<String, Object> configProps = new HashMap<>(consumerFactory.getConfigurationProperties());
        configProps.remove(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG);
        configProps.remove(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG);
        configProps.keySet().removeIf(key ->
                key.contains("spring.json") || key.contains("json.trusted.packages")
        );
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        // =================================================================
        // 2. 🎯 第一阶段核心：物理层绝对安全，无条件解密为 String
        // =================================================================
        // 底层直接将 Value 解密为最通用的 String，这样绝对不会抛出 ClassNotFoundException
        DefaultKafkaConsumerFactory<String, Object> customConsumerFactory =
                new DefaultKafkaConsumerFactory<>(configProps);
        // 🎯 修复泛型红线：将 StringDeserializer 强转为通用的 Deserializer
        customConsumerFactory.setKeyDeserializer(new StringDeserializer());
        // 🎯 修复：利用 Lambda 表达式直接返回数据，完美欺骗并对齐 Deserializer<Object> 泛型
        customConsumerFactory.setValueDeserializer((topic, data) -> {
            if (data == null) {
                return null;
            }
            return new String(data, java.nio.charset.StandardCharsets.UTF_8);
        });

        factory.setConsumerFactory(customConsumerFactory);

        // =================================================================
        // 3. 🎯 第二阶段核心：挂载智能消息转换器（JsonMessageConverter）
        //    它会根据你的 @KafkaListener(id=...) 方法签名，动态将上面的 String 转为目标 DTO
        // =================================================================

        org.springframework.kafka.support.converter.JsonMessageConverter recordConverter =
                new org.springframework.kafka.support.converter.JsonMessageConverter(objectMapper);

        // 2. 用专门的批量转换器将其包裹，这样它才能识别并遍历 List 中的每一条 String
//        org.springframework.kafka.support.converter.BatchMessagingMessageConverter batchConverter =
//                new org.springframework.kafka.support.converter.BatchMessagingMessageConverter(recordConverter);

        //factory.setBatchMessageConverter(batchConverter);
        factory.setRecordMessageConverter(recordConverter);

        // =================================================================
        // 4. 基础行为控制（单条消费、手动提交、企业级容错）
        // =================================================================
        factory.setBatchListener(false);

        // 既然是核心业务通用工厂，严格建议采用 MANUAL_IMMEDIATE（消费完立刻手动提交）
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        // 健壮的重试机制：业务报错时，间隔 5 秒，重试 2 次
        factory.setCommonErrorHandler(
                new DefaultErrorHandler(
                        new FixedBackOff(5000L, 2)));
        return factory;
    }


    @Bean("broadcastKafkaFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> broadcastKafkaFactory(
            ConsumerFactory<String, Object> baseConsumerFactory, ObjectMapper objectMapper) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        // =================================================================
        // 1. 清理全局可能污染的 spring.json 配置
        // =================================================================
        Map<String, Object> configProps = new HashMap<>(baseConsumerFactory.getConfigurationProperties());
        configProps.remove(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG);
        configProps.remove(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG);
        configProps.keySet().removeIf(key ->
                key.contains("spring.json") ||
                        key.contains("json.trusted.packages")
        );

        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        // 底层直接将 Value 解密为最通用的 String，这样绝对不会抛出 ClassNotFoundException
        DefaultKafkaConsumerFactory<String, Object> customConsumerFactory =
                new DefaultKafkaConsumerFactory<>(configProps);
        // 🎯 修复泛型红线：将 StringDeserializer 强转为通用的 Deserializer
        customConsumerFactory.setKeyDeserializer(new StringDeserializer());
        // 🎯 修复：利用 Lambda 表达式直接返回数据，完美欺骗并对齐 Deserializer<Object> 泛型
        customConsumerFactory.setValueDeserializer((topic, data) -> {
            if (data == null) {
                return null;
            }
            return new String(data, java.nio.charset.StandardCharsets.UTF_8);
        });

        factory.setConsumerFactory(customConsumerFactory);

        // =================================================================
        // 3. 🎯 核心配置：挂载全能型消息转换器（JsonMessageConverter）
        //    当消息被消费出来后，这个转换器会“偷偷”看一眼具体的 @KafkaListener 方法签名
        //    如果方法写的是 MerchantStaticDTO，它就负责把 String 揉成 MerchantStaticDTO
        // =================================================================
        factory.setRecordMessageConverter(new org.springframework.kafka.support.converter.JsonMessageConverter(objectMapper));

        // =================================================================
        // 4. 广播组与手动提交策略定制
        // =================================================================
        factory.getContainerProperties().setGroupId("stat-group-bc-" + UUID.randomUUID());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    @Autowired
    private KafkaProperties kafkaProperties;


    @Bean("customProducerFactory")
    @Primary
    public ProducerFactory<String, Object> customProducerFactory() {
        // 1. 🎯 降维打击：直接从 YML 对应的属性类里提取原本的生产配置字典（包含服务器连接地址）
        Map<String, Object> configProps = this.kafkaProperties.buildProducerProperties(null);

        // 2. 创建并定制专属的 JsonSerializer，强行抹除 __TypeId__
        JsonSerializer<Object> jsonSerializer = new JsonSerializer<>();
        jsonSerializer.setAddTypeInfo(false);

        // 3. 构造全新物理隔离的工厂
        DefaultKafkaProducerFactory<String, Object> factory =
                new DefaultKafkaProducerFactory<>(configProps);

        factory.setKeySerializer(new StringSerializer());
        factory.setValueSerializer(jsonSerializer);

        return factory;
    }

    @Bean
    @Primary
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> customProducerFactory) {
        return new KafkaTemplate<>(customProducerFactory);
    }
}