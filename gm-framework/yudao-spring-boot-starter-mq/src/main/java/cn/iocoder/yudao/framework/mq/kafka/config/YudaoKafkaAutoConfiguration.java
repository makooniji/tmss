package cn.iocoder.yudao.framework.mq.kafka.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("cn.iocoder.yudao.framework.mq.kafka") // 确保扫描到该包
public class YudaoKafkaAutoConfiguration {
}