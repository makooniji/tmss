package cn.iocoder.yudao.framework.common.util.date;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 时区转换工具类
 * <p>
 * 用于将客户端传入的时区日期字符串转换为数据库查询所用的日期字符串。
 * 目标时区默认为数据库时区 Asia/Shanghai。
 * <p>
 * 使用示例（客户端 America/Los_Angeles，数据库 Asia/Shanghai）：
 * <pre>
 *   convertDayStart("2026-06-08", "America/Los_Angeles") → "2026-06-08"
 *   convertDayEnd  ("2026-06-09", "America/Los_Angeles") → "2026-06-10"
 * </pre>
 */
public class TimeZoneUtils {

    /**
     * 数据库连接配置使用 serverTimezone=Asia/Shanghai，日期查询统一转换到该时区。
     */
    private static final String DATABASE_ZONE = "Asia/Shanghai";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 将日期字符串"当天起始时刻"从 fromZone 转换到数据库时区，返回转换后的日期字符串。
     * 用于范围查询的开始日期转换。
     *
     * @param dateStr  日期字符串，格式 yyyy-MM-dd
     * @param fromZone 源时区 ID，如 "Asia/Shanghai"；为 null 或空时原样返回
     * @return 数据库时区下的日期字符串
     */
    public static String convertDayStart(String dateStr, String fromZone) {
        return convertDayStart(dateStr, fromZone, DATABASE_ZONE);
    }

    /**
     * 将日期字符串"当天起始时刻"从 fromZone 转换到 toZone，返回转换后的日期字符串。
     *
     * @param dateStr  日期字符串，格式 yyyy-MM-dd
     * @param fromZone 源时区 ID，如 "Asia/Shanghai"；为 null 或空时原样返回
     * @param toZone   目标时区 ID，如 "UTC"
     * @return 目标时区下的日期字符串
     */
    public static String convertDayStart(String dateStr, String fromZone, String toZone) {
        if (dateStr == null || fromZone == null || fromZone.isBlank()) {
            return dateStr;
        }
        ZoneId from = ZoneId.of(fromZone);
        ZoneId to = ZoneId.of(toZone);
        LocalDate localDate = LocalDate.parse(dateStr, DATE_FORMATTER);
        ZonedDateTime startOfDay = localDate.atStartOfDay(from);
        return startOfDay.withZoneSameInstant(to).toLocalDate().format(DATE_FORMATTER);
    }

    /**
     * 将日期字符串"当天结束时刻"从 fromZone 转换到数据库时区，返回转换后的日期字符串。
     * 用于范围查询的结束日期转换。
     *
     * @param dateStr  日期字符串，格式 yyyy-MM-dd
     * @param fromZone 源时区 ID，如 "Asia/Shanghai"；为 null 或空时原样返回
     * @return 数据库时区下的日期字符串
     */
    public static String convertDayEnd(String dateStr, String fromZone) {
        return convertDayEnd(dateStr, fromZone, DATABASE_ZONE);
    }

    /**
     * 将日期字符串"当天结束时刻"从 fromZone 转换到 toZone，返回转换后的日期字符串。
     *
     * @param dateStr  日期字符串，格式 yyyy-MM-dd
     * @param fromZone 源时区 ID，如 "Asia/Shanghai"；为 null 或空时原样返回
     * @param toZone   目标时区 ID，如 "UTC"
     * @return 目标时区下的日期字符串
     */
    public static String convertDayEnd(String dateStr, String fromZone, String toZone) {
        if (dateStr == null || fromZone == null || fromZone.isBlank()) {
            return dateStr;
        }
        ZoneId from = ZoneId.of(fromZone);
        ZoneId to = ZoneId.of(toZone);
        LocalDate localDate = LocalDate.parse(dateStr, DATE_FORMATTER);
        ZonedDateTime endOfDay = localDate.atTime(LocalTime.MAX).atZone(from);
        return endOfDay.withZoneSameInstant(to).toLocalDate().format(DATE_FORMATTER);
    }
}
