package cn.iocoder.yudao.module.game.adapter.sports.saba.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;


import java.io.IOException;


public class GzipFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String encoding = httpRequest.getHeader("Content-Encoding");

        if (encoding != null && encoding.contains("gzip")) {
            // 这里替换了 Request 对象，后续所有的逻辑（包括 Controller）拿到的都是这个 wrapper
            GzipRequestWrapper wrapper = new GzipRequestWrapper(httpRequest);
            chain.doFilter(wrapper, response);
        } else {
            chain.doFilter(request, response);
        }
    }
}