package cn.iocoder.yudao.framework.web.zone;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ZoneUtils {

    // 缓存全局忽略大小写的 ZoneId 映射表，提高解析性能
    private static final Map<String, ZoneId> CASE_INSENSITIVE_ZONE_MAP = new ConcurrentHashMap<>();

    static {
        for (String zoneId : ZoneId.getAvailableZoneIds()) {
            CASE_INSENSITIVE_ZONE_MAP.put(zoneId.toLowerCase(), ZoneId.of(zoneId));
        }
    }

    /**
     * 安全解析 ZoneId，支持忽略大小写（如 ASIA/KOLKATA -> Asia/Kolkata）
     */
    public static ZoneId parseZoneId(String zoneStr) {
        if (StrUtil.isBlank(zoneStr)) {
            return ZoneId.systemDefault();
        }

        // 1. 优先尝试标准解析
        try {
            return ZoneId.of(zoneStr);
        } catch (Exception ignored) {
            // 忽略异常，进入忽略大小写兜底匹配
        }

        // 2. 忽略大小写匹配 (例如 ASIA/KOLKATA 或 asia/kolkata)
        ZoneId matchedZone = CASE_INSENSITIVE_ZONE_MAP.get(zoneStr.toLowerCase());
        if (matchedZone != null) {
            return matchedZone;
        }

        // 3. 解析失败降级，避免系统报错抛异常
        log.warn("[ZoneUtils] 未知的时区标识: {}, 降级使用系统默认时区: {}", zoneStr, ZoneId.systemDefault());
        return ZoneId.systemDefault();
    }
}