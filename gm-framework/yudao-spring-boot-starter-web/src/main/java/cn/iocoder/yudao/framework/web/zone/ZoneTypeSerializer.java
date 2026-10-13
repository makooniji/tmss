package cn.iocoder.yudao.framework.web.zone;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.json.databind.TimestampLocalDateTimeSerializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Component
public class ZoneTypeSerializer extends JsonSerializer<Object> implements ContextualSerializer {

    private String pattern = "yyyy-MM-dd HH:mm:ss";

    public ZoneTypeSerializer() {}

    public ZoneTypeSerializer(String pattern) {
        this.pattern = pattern;
    }
    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        // 从 ThreadLocal 获取前端传入的时区 (例如 "America/Los_Angeles")
        String targetZoneStr = ZoneContextHolder.getZone();

        // 【降级处理】：如果前端未传递 zone 参数，直接调用系统全局的时间戳序列化器
        if (StrUtil.isBlank(targetZoneStr)) {
            if (value instanceof LocalDateTime) {
                TimestampLocalDateTimeSerializer.INSTANCE.serialize((LocalDateTime) value, gen, serializers);
                return;
            }
        }

        // 前端有传递 zone 参数时，按指定时区格式化为字符串输出
        try {
            ZoneId targetZone = ZoneId.of(targetZoneStr);
            ZoneId sysZone = ZoneId.systemDefault();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

            if (value instanceof LocalDateTime) {
                LocalDateTime ldt = (LocalDateTime) value;
                String formatted = ldt.atZone(sysZone)
                        .withZoneSameInstant(targetZone)
                        .format(formatter);
                gen.writeString(formatted);
                return;
            }
        } catch (Exception ignored) {
            // 异常降级回时间戳
        }

        gen.writeObject(value);
    }

    private void writeDefault(Object value, JsonGenerator gen) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        if (value instanceof LocalDateTime) {
            gen.writeString(((LocalDateTime) value).format(formatter));
        } else if (value instanceof Date) {
            gen.writeString(((Date) value).toInstant().atZone(ZoneId.systemDefault()).format(formatter));
        } else {
            gen.writeObject(value);
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        if (property != null) {
            ZoneType annotation = property.getAnnotation(ZoneType.class);
            if (annotation != null) {
                return new ZoneTypeSerializer(annotation.pattern());
            }
        }
        return prov.findValueSerializer(property.getType(), property);
    }
}