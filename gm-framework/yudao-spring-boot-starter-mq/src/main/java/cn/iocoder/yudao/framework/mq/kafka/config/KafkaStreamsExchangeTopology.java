package cn.iocoder.yudao.framework.mq.kafka.config;


import cn.iocoder.yudao.framework.mq.kafka.KafkaTopicConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.processor.api.FixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorContext;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.TopicBuilder;

import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
@EnableKafkaStreams // 🎯 关键：开启 Spring Boot 对 Kafka Streams 的自动装配
public class KafkaStreamsExchangeTopology {
    @Autowired
    private KafkaTopicConfig kafkaTopicConfig;

    /**
     * 🎯 自动创建上游的统一交换机大管道 Topic
     */
    @Bean
    public NewTopic universalExchangeTopic() {
        return TopicBuilder.name(kafkaTopicConfig.getUniversalExchangeTopic())
                .partitions(3)            // 根据生产环境并发量调整分区数
                .replicas(1)              // 副本数（单机测试用1，生产环境通常设为3）
                .build();
    }

    /**
     * 🎯 自动创建下游路由的统计物理 Topic
     */
    @Bean
    public NewTopic statTopic() {
        return TopicBuilder.name(kafkaTopicConfig.getStatTopic())
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * 🎯 自动创建死信兜底 Topic
     */
    @Bean
    public NewTopic deadLetterTopic() {
        return TopicBuilder.name("exchange-dead-letter-topic")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Data
    @AllArgsConstructor
    public static class RoutedRecord {
        private String routingKey; // 从 Header 中剥离出来的路由键
        private String payload;    // 原始的 JSON 消息体
    }

    /**
     * 定义一个 KStream 处理器。Spring 会自动注入名为 'defaultKafkaStreamsBuilder' 的工厂
     */
    @Bean
    public KStream<String, String> kStreamExchange(StreamsBuilder streamsBuilder) {

        // 1. 接入统一交换机大管道的原始文本流
        KStream<String, String> sourceStream = streamsBuilder.stream(
                kafkaTopicConfig.getUniversalExchangeTopic(),
                Consumed.with(Serdes.String(), Serdes.String())
        );

        // 2. 🎯 核心破局：拦截字节流，利用底层 Processor API 剥离出 Header 存入包装对象
        KStream<String, RoutedRecord> routedStream = sourceStream.processValues(() ->
                new FixedKeyProcessor<String, String, RoutedRecord>() {
                    private FixedKeyProcessorContext<String, RoutedRecord> context;

                    @Override
                    public void init(FixedKeyProcessorContext<String, RoutedRecord> context) {
                        this.context = context;
                    }

                    @Override
                    public void process(FixedKeyRecord<String, String> record) {
                        String routingKey = "";
                        var header = record.headers().lastHeader("routing_key");
                        if (header != null) {
                            routingKey = new String(header.value(), StandardCharsets.UTF_8);
                        }
                        // 向下传递包装后的对象
                        context.forward(record.withValue(new RoutedRecord(routingKey, record.value())));
                    }
                }
        );

        // 3. 🎯 利用高级 DSL 算子进行动态多 Topic 路由分发 (等同于你的 4 次 send)
        routedStream.split(Named.as("exchange-"))
                // 只要路由键匹配，无条件分发。支持通配符或精准匹配
                .branch((key, routedRecord) -> "tenant.static.update".equals(routedRecord.getRoutingKey()),
                        Branched.withConsumer(stream -> {
                            // 一个分支可以同时写出到多个物理 Topic，完美平替你原先的串行发送
                            stream.mapValues(RoutedRecord::getPayload).to(kafkaTopicConfig.getStatTopic());
                        }))

                // 默认兜底分支（防止未知消息卡死流）
                .defaultBranch(Branched.withConsumer(stream -> stream
                        .mapValues(RoutedRecord::getPayload)
                        .to("exchange-dead-letter-topic")));

        return sourceStream;
    }
}