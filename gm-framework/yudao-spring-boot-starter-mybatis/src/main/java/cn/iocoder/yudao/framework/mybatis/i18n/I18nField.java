package cn.iocoder.yudao.framework.mybatis.i18n;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface I18nField {

    /**
     * JSON 来源字段
     */
    String source() default "lang";

    /**
     * JSON key
     */
    String key();

    /**
     * 默认语言
     */
    String fallback() default "en";
}