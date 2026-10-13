package cn.iocoder.yudao.framework.web.zone;

import cn.iocoder.yudao.framework.web.coinType.CoinTypeContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class ZoneInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 优先读取 Header，其次读取 Query 参数
        String currency = request.getHeader("X-Zone");
        if (!StringUtils.hasText(currency)) {
            currency = request.getParameter("zone");
        }

        if (StringUtils.hasText(currency)) {
            ZoneContextHolder.setZone(currency.toUpperCase());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ZoneContextHolder.clear();
    }
}