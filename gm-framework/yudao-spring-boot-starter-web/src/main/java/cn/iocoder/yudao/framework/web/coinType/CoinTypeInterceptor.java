package cn.iocoder.yudao.framework.web.coinType;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;



@Component
public class CoinTypeInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 优先读取 Header，其次读取 Query 参数
        String currency = request.getHeader("X-CoinType-Currency");
        if (!StringUtils.hasText(currency)) {
            currency = request.getParameter("coinType");
        }

        if (StringUtils.hasText(currency)) {
            CoinTypeContextHolder.setCurrency(currency.toUpperCase());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束后清理 ThreadLocal，防止内存泄漏和线程污染
        CoinTypeContextHolder.clear();
    }
}