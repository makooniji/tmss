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

/**
 * 针对 GET / Query 参数的 ZoneType 自动转换器
 */
public class ZoneTypeAnnotationFormatterFactory implements AnnotationFormatterFactory<ZoneType> {

    @Override
    public Set<Class<?>> getFieldTypes() {
        // 声明目标类型为 LocalDateTime
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
            text = text.replace("+", " "); // 解决 URL 中空格变 + 的问题
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(annotation.pattern());

            String clientZoneStr = ZoneContextHolder.getZone();
            if (StrUtil.isBlank(clientZoneStr)) {
                return LocalDateTime.parse(text, formatter);
            }

            try {
                ZoneId clientZone = ZoneId.of(clientZoneStr);
                ZoneId sysZone = ZoneId.systemDefault();
                // 客户端时区时间 -> 转为系统时区 LocalDateTime
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