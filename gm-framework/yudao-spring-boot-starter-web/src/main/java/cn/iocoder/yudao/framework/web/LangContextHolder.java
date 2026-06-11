package cn.iocoder.yudao.framework.web;

import com.alibaba.ttl.TransmittableThreadLocal;

public class LangContextHolder {

    private static final ThreadLocal<String> HOLDER = new TransmittableThreadLocal<>();

    public static void set(String lang) {
        HOLDER.set(lang);
    }

    public static String get() {
        return HOLDER.get() == null ? "en-US" : HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}