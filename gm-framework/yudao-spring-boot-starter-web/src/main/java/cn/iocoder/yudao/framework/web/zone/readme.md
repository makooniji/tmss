# Spring Boot 动态时区自动转换组件（ZoneType）集成指南

## 1. 组件概述

在跨国/多租户应用中，客户端（如 Web、App、不同国家商户）传入的时间通常为其本地时区时间，而后端数据库与服务器统一使用系统时区（如 `Asia/Shanghai`）。

`ZoneType` 组件用于解决前端请求时间与后端服务器时区不一致的问题：

* **自动拦截提取**：通过 HTTP Header 自动读取客户端时区（如 `zone: Asia/Kolkata`）。
* **全请求链路支持**：同时支持 **GET (Query/Form)** 与 **POST (JSON Body)** 参数的时间格式自动转换。
* **时区容错处理**：兼容不规范的时区字符串（如全大写 `ASIA/KOLKATA` 或 `asia/kolkata`），防止时区异常导致系统崩溃。

---

## 2. 核心代码组件说明

### 2.1 时区注解定义 (`ZoneType.java`)

用于标注在请求 VO 的日期/时间字段上。

```java
package cn.iocoder.yudao.framework.web.zone;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@JacksonAnnotationsInside
@JsonDeserialize(using = ZoneTypeDeserializer.class)
public @interface ZoneType {

    /**
     * 时间格式，默认为 yyyy-MM-dd HH:mm:ss
     */
    String pattern() default "yyyy-MM-dd HH:mm:ss";
}

```

---

### 2.2 上下文容器 (`ZoneContextHolder.java`)

基于 `ThreadLocal` 传递当前请求的时区信息。

```java
package cn.iocoder.yudao.framework.web.zone;

import cn.hutool.core.util.StrUtil;

public class ZoneContextHolder {

    private static final ThreadLocal<String> ZONE_HOLDER = new ThreadLocal<>();

    public static void setZone(String zone) {
        if (StrUtil.isNotBlank(zone)) {
            ZONE_HOLDER.set(zone.trim());
        }
    }

    public static String getZone() {
        return ZONE_HOLDER.get();
    }

    public static void clear() {
        ZONE_HOLDER.remove();
    }
}

```

---

### 2.3 时区解析工具类 (`ZoneUtils.java`)

支持 **忽略大小写** 解析 IANA 时区标识（如 `ASIA/KOLKATA` -> `Asia/Kolkata`），避免非法时区报错。

```java
package cn.iocoder.yudao.framework.web.zone;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ZoneUtils {

    private static final Map<String, ZoneId> CASE_INSENSITIVE_ZONE_MAP = new ConcurrentHashMap<>();

    static {
        for (String zoneId : ZoneId.getAvailableZoneIds()) {
            CASE_INSENSITIVE_ZONE_MAP.put(zoneId.toLowerCase(), ZoneId.of(zoneId));
        }
    }

    /**
     * 安全解析 ZoneId，支持忽略大小写（如 ASIA/KOLKATA）
     */
    public static ZoneId parseZoneId(String zoneStr) {
        if (StrUtil.isBlank(zoneStr)) {
            return ZoneId.systemDefault();
        }

        try {
            return ZoneId.of(zoneStr);
        } catch (Exception ignored) {
            // 进入忽略大小写匹配
        }

        ZoneId matchedZone = CASE_INSENSITIVE_ZONE_MAP.get(zoneStr.toLowerCase());
        if (matchedZone != null) {
            return matchedZone;
        }

        log.warn("[ZoneUtils] 未知时区: {}, 降级使用系统默认时区: {}", zoneStr, ZoneId.systemDefault());
        return ZoneId.systemDefault();
    }
}

```

---

### 2.4 请求拦截器 (`ZoneInterceptor.java`)

拦截 HTTP 请求，从 Header 中解析 `zone` 存入 ThreadLocal。

```java
package cn.iocoder.yudao.framework.web.zone;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class ZoneInterceptor implements HandlerInterceptor {

    public static final String HEADER_ZONE = "zone";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String zone = request.getHeader(HEADER_ZONE);
        if (zone != null) {
            ZoneContextHolder.setZone(zone);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ZoneContextHolder.clear();
    }
}

```

---

### 2.5 GET/Query 转换器 (`ZoneTypeAnnotationFormatterFactory.java`)

处理 `GET` 请求参数绑定的时区转换。

```java
package cn.iocoder.yudao.framework.web.zone;

import cn.hutool.core.util.StrUtil;
import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Parser;
import org.springframework.format.Printer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;

public class ZoneTypeAnnotationFormatterFactory implements AnnotationFormatterFactory<ZoneType> {

    @Override
    public Set<Class<?>> getFieldTypes() {
        return Collections.singleton(LocalDateTime.class);
    }

    @Override
    public Printer<?> getPrinter(ZoneType annotation, Class<?> fieldType) {
        return (object, locale) -> {
            if (object instanceof LocalDateTime) {
                return ((LocalDateTime) object).format(DateTimeFormatter.ofPattern(annotation.pattern()));
            }
            return "";
        };
    }

    @Override
    public Parser<?> getParser(ZoneType annotation, Class<?> fieldType) {
        return (text, locale) -> {
            if (StrUtil.isBlank(text)) {
                return null;
            }
            text = text.replace("+", " ");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(annotation.pattern());
            String clientZoneStr = ZoneContextHolder.getZone();

            if (StrUtil.isBlank(clientZoneStr)) {
                return LocalDateTime.parse(text, formatter);
            }

            try {
                ZoneId clientZone = ZoneUtils.parseZoneId(clientZoneStr);
                ZoneId sysZone = ZoneId.systemDefault();
                return LocalDateTime.parse(text, formatter)
                        .atZone(clientZone)
                        .withZoneSameInstant(sysZone)
                        .toLocalDateTime();
            } catch (Exception e) {
                return LocalDateTime.parse(text, formatter);
            }
        };
    }
}

```

---

### 2.6 POST/JSON 反序列化器 (`ZoneTypeStringDeserializer.java`)

处理 `POST` 请求 Body 中 JSON 格式字符串转换。

```java
package cn.iocoder.yudao.framework.web.zone;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ZoneTypeStringDeserializer extends JsonDeserializer<String> implements ContextualDeserializer {

    private String pattern = "yyyy-MM-dd HH:mm:ss";

    public ZoneTypeStringDeserializer() {
    }

    public ZoneTypeStringDeserializer(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateStr = p.getText();
        if (StrUtil.isBlank(dateStr)) {
            return dateStr;
        }

        String clientZoneStr = ZoneContextHolder.getZone();
        if (StrUtil.isBlank(clientZoneStr)) {
            return dateStr;
        }

        try {
            dateStr = dateStr.replace("+", " ");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            ZoneId clientZone = ZoneUtils.parseZoneId(clientZoneStr);
            ZoneId sysZone = ZoneId.systemDefault();

            return LocalDateTime.parse(dateStr, formatter)
                    .atZone(clientZone)
                    .withZoneSameInstant(sysZone)
                    .format(formatter);
        } catch (Exception ignored) {
            return dateStr;
        }
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        if (property != null) {
            ZoneType annotation = property.getAnnotation(ZoneType.class);
            if (annotation != null) {
                return new ZoneTypeDeserializer(annotation.pattern());
            }
        }
        return this;
    }
}

```

---

### 2.7 Spring MVC 自动配置整合 (`YudaoWebAutoConfiguration.java`)

```java
@AutoConfiguration
@EnableConfigurationProperties(WebProperties.class)
public class YudaoWebAutoConfiguration {

    @Bean
    public ZoneInterceptor zoneInterceptor() {
        return new ZoneInterceptor();
    }

    @Bean
    public ZoneTypeAnnotationFormatterFactory zoneTypeAnnotationFormatterFactory() {
        return new ZoneTypeAnnotationFormatterFactory();
    }

    @Bean
    public WebMvcConfigurer yudaoWebMvcConfigurer(ZoneInterceptor zoneInterceptor,
                                                 ZoneTypeAnnotationFormatterFactory zoneTypeAnnotationFormatterFactory) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(zoneInterceptor).addPathPatterns("/**");
            }

            @Override
            public void addFormatters(FormatterRegistry registry) {
                registry.addFormatterForFieldAnnotation(zoneTypeAnnotationFormatterFactory);
            }
        };
    }
}

```

---

## 3. 使用规范与示例

### 3.1 请求 VO 注解标注

* **POST JSON 请求（字段为 String）**：
```java
@Data
public class GameReportReqVO {
    @ZoneType
    private String startTime; // 客户端时间格式字符串，如 "2026-09-01 00:00:00"
}

```


* **GET Query 请求（推荐字段为 LocalDateTime）**：
```java
@Data
public class GameReportPageReqVO extends PageParam {
    @ZoneType
    private LocalDateTime startTime;
}

```



### 3.2 客户端 Request 报文示例

前端发起 HTTP 请求时，必须在 **Header** 中增加 `zone` 参数：

```http
GET /admin-api/game/page?startTime=2026-09-01 00:00:00 HTTP/1.1
Host: api.example.com
zone: Asia/Kolkata

```

---

## 4. 常见问题与排查 Checklist

| 现象 | 排查方向 | 解决方案 |
| --- | --- | --- |
| **POST 请求时间未转换** | `zone` 参数是否放在 Body 中？ | 必须统一放在 **Header**（`zone: Asia/Kolkata`），确保拦截器提前写入 ThreadLocal。 |
| **GET 请求未生效** | VO 字段是否为 `String`？ | Spring DataBinder 遇到同类型直接赋值，推荐将 GET VO 的字段类型改为 `LocalDateTime`。 |
| **报错 Unknown time-zone ID** | 客户端传入大小写不符合规范（如 `ASIA/KOLKATA`） | 确认使用了 `ZoneUtils.parseZoneId()` 兼容忽略大小写逻辑。 |
| **空格变成 `+**` | URL 未正确做 URL Encode | 代码内部已内置 `.replace("+", " ")` 兼容处理。 |