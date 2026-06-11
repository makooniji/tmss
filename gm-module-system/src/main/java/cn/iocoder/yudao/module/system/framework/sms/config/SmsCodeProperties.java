package cn.iocoder.yudao.module.system.framework.sms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@ConfigurationProperties(prefix = "yudao.sms-code")
@Validated
@Data
public class SmsCodeProperties {

    /**
     * 过期时间
     */

    private Duration expireTimes;
    /**
     * 短信发送频率
     */

    private Duration sendFrequency;
    /**
     * 每日发送最大数量
     */

    private Integer sendMaximumQuantityPerDay;
    /**
     * 验证码最小值
     */

    private Integer beginCode;
    /**
     * 验证码最大值
     */

    private Integer endCode;

}
