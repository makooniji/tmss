package cn.iocoder.yudao.framework.web.coinType;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@JacksonAnnotationsInside // 告诉 Jackson 这是组合注解
@JsonSerialize(using = CoinTypeSerializer.class) // 指定序列化器
public @interface CoinType {

}