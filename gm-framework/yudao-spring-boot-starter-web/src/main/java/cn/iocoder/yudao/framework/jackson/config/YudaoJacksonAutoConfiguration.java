package cn.iocoder.yudao.framework.jackson.config;

import cn.iocoder.yudao.framework.common.util.json.databind.NumberSerializer;
import cn.iocoder.yudao.framework.common.util.json.databind.TimestampLocalDateTimeDeserializer;
import cn.iocoder.yudao.framework.common.util.json.databind.TimestampLocalDateTimeSerializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.JsonNodeFeature;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.fhs.core.trans.vo.VO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@AutoConfiguration(after = JacksonAutoConfiguration.class)
@Slf4j
public class YudaoJacksonAutoConfiguration {

    /**
     * VO MixIn：全局过滤 transMap 属性
     * 由于 Easy-Trans 会添加 transMap 属性，避免 Jackson 在序列化时包含该字段
     */
    @JsonIgnoreProperties(value = "transMap")
    public interface VOMixIn {
    }

    /**
     * 【修复核心点 1】：删除了原本产生冲突的 @Bean @Primary ObjectMapper 方法！
     * 不再去硬造一个对象，而是改为拦截加工。
     *
     * 通过实现 SmartInitializingSingleton，在 Spring 容器把所有单例 Bean（包括别人造出来的那个 primary）
     * 塞进容器后的最终阶段，捞出来注入我们必须依赖的那些底层 Feature 策略。
     */
    @Bean
    public SmartInitializingSingleton configureExistingObjectMapper(ObjectMapper objectMapper) {
        return () -> {
            log.info("[Jackson配置] 正在对系统唯一的 Primary ObjectMapper 织入全局精度与过滤特性...");

            // 1. 配置基础特性
            objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

            // 2. 设置全局包含策略（忽略 null 以及 Map 内部的 null）
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.setDefaultPropertyInclusion(
                    JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.NON_NULL)
            );

            // ==================== 💎 BigDecimal 精度安全区 ====================
            // [反序列化] 读数据时：强行用 BigDecimal 接收，保留 "18.00" 尾部的零
            objectMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

            // [序列化] 写数据时：强制以纯文本形式输出（绝对禁止转成科学计数法）
            objectMapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);

            // [树模型优化] 完美修正底层报错：通过配置类入口关闭“树结构抹除末尾零”特性
            objectMapper.configure(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES, false);
        };
    }

    /**
     * 【修复核心点 2】：专属定制器继续保留。
     * 框架在调用 builder 创建任何新的或者已有的序列化器时，都会把下面的这些类型转换规则自动洗进去。
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer ldtEpochMillisCustomizer() {
        return builder -> builder
                // 全局过滤 VO 的 transMap 属性
                .mixIn(VO.class, VOMixIn.class)

                // Long -> Number（避免前端精度丢失）
                .serializerByType(Long.class, NumberSerializer.INSTANCE)
                .serializerByType(Long.TYPE, NumberSerializer.INSTANCE)

                // LocalDate / LocalTime
                .serializerByType(LocalDate.class, LocalDateSerializer.INSTANCE)
                .deserializerByType(LocalDate.class, LocalDateDeserializer.INSTANCE)
                .serializerByType(LocalTime.class, LocalTimeSerializer.INSTANCE)
                .deserializerByType(LocalTime.class, LocalTimeDeserializer.INSTANCE)

                // LocalDateTime < - > EpochMillis
                .serializerByType(LocalDateTime.class, TimestampLocalDateTimeSerializer.INSTANCE)
                .deserializerByType(LocalDateTime.class, TimestampLocalDateTimeDeserializer.INSTANCE)

                // 保留两位小数点
                //.serializerByType(BigDecimal.class, new BigDecimalTwoDegreeSerializer())
                ;
    }
}