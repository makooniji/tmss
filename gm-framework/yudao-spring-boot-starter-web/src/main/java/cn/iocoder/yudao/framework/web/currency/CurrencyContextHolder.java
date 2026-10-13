package cn.iocoder.yudao.framework.web.currency;

import com.alibaba.ttl.TransmittableThreadLocal;

public class CurrencyContextHolder {
    private static final ThreadLocal<String> CURRENT_CURRENCY = new TransmittableThreadLocal<>();

    public static void setCurrency(String currency) {
        CURRENT_CURRENCY.set(currency);
    }

    public static String getCurrency() {
        // 默认返回 CNY
        return CURRENT_CURRENCY.get() == null ? "" : CURRENT_CURRENCY.get();
    }

    public static void clear() {
        CURRENT_CURRENCY.remove();
    }
}