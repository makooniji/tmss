package cn.iocoder.yudao.framework.mq.kafka;

public interface KafkaTopics {
    String STAT_TOPIC = "tenant_day_stat";
    String USER_TOPIC = "tenant_user_stat";

    //商户 用户注册
    String MERCHANT_USER_REGISTER = "merchant_user_register";
    String MERCHANT_USER_REPORT = "merchant_user_total_stat";


}