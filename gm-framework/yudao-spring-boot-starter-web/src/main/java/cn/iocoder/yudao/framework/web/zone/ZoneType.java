package cn.iocoder.yudao.framework.web.zone;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@JacksonAnnotationsInside // 告诉 Jackson 这是组合注解
@JsonSerialize(using = ZoneTypeSerializer.class) // 指定序列化器
@JsonDeserialize(using = ZoneTypeDeserializer.class)
public @interface ZoneType {
    /**
     * 时间输出格式，默认 yyyy-MM-dd HH:mm:ss
     */
    String pattern() default "yyyy-MM-dd HH:mm:ss";
}