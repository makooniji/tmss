package cn.iocoder.yudao.module.game.adapter.sports.saba.config;


import cn.iocoder.yudao.framework.common.enums.WebFilterOrderEnum;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {


    @Bean
    public FilterRegistrationBean<GzipFilter> gzipFilterRegistration() {
        FilterRegistrationBean<GzipFilter> registration = new FilterRegistrationBean<>();

        // 1. 注入你写的过滤器（见下方第2步）
        registration.setFilter(new GzipFilter());

        // 2. 指定拦截路径，和你之前在 Interceptor 里的路径保持一致
        registration.addUrlPatterns("/gateway/notify/sports/saba/*");

        // 3. 设置名称和执行顺序（数字越小越先执行，建议设为 1）
        registration.setName("gzipFilter");
        registration.setOrder(WebFilterOrderEnum.API_ACCESS_LOG_FILTER-2);

        // 4. 确保在 REQUEST 类型下触发
        registration.setDispatcherTypes(DispatcherType.REQUEST);

        return registration;
    }


}