package cn.iocoder.yudao.framework.web.zone;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class ZoneTypeDeserializer extends JsonDeserializer<LocalDateTime> implements ContextualDeserializer {

    private String pattern = "yyyy-MM-dd HH:mm:ss";

    public ZoneTypeDeserializer() {}

    public ZoneTypeDeserializer(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateStr = p.getText();
        if (StrUtil.isBlank(dateStr)) {
            return null;
        }

        // 从 ThreadLocal 获取前端传入的时区 (如 "America/Los_Angeles")
        String clientZoneStr = ZoneContextHolder.getZone();
        if (StrUtil.isBlank(clientZoneStr)) {
            return null;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            ZoneId clientZone = ZoneId.of(clientZoneStr);
            ZoneId sysZone = ZoneId.systemDefault();

            // 解析客户端时区时间 -> 转为系统时区时间 -> 格式化回字符串
            return LocalDateTime.parse(dateStr, formatter)
                    .atZone(clientZone)
                    .withZoneSameInstant(sysZone)
                    .toLocalDateTime();
        } catch (Exception ignored) {
            return null; // 解析异常降级原样返回
        }
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        if (property != null) {
            ZoneType annotation = property.getAnnotation(ZoneType.class);
            if (annotation != null) {
                return new ZoneTypeDeserializer(annotation.pattern());
            }
        }
        return this;
    }
}