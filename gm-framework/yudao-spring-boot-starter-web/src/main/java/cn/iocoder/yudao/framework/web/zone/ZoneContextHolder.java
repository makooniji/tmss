package cn.iocoder.yudao.framework.web.zone;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.time.ZoneId;

public class ZoneContextHolder {
    private static final ThreadLocal<String> CURRENT_ZONE = new TransmittableThreadLocal<>();

    public static void setZone(String zone) {
        CURRENT_ZONE.set(zone);
    }

    public static String getZone() {
       String zone= CURRENT_ZONE.get() == null ? ZoneId.systemDefault().toString() : CURRENT_ZONE.get();
       return ZoneUtils.parseZoneId(zone).toString();
    }

    public static void clear() {
        CURRENT_ZONE.remove();
    }
}