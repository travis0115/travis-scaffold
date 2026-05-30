package com.travis.infrastructure.framework.web.config;

import com.travis.infrastructure.common.web.constant.CustomHttpHeaders;
import com.travis.infrastructure.common.web.constant.WebFilterOrders;
import com.travis.infrastructure.framework.web.core.exception.advice.CommonExceptionHandlerAdvice;
import com.travis.infrastructure.framework.web.core.filter.RequestContextFilter;
import com.travis.infrastructure.framework.web.core.filter.RequestIdFilter;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 自动配置类
 *
 * @author travis
 */
@AutoConfiguration
public class TravisWebMvcAutoConfiguration implements WebMvcConfigurer {

    /**
     * 跨域处理
     * 若使用 Spring Security,开启 http.cors(withDefaults())，会自动启用该配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true)
                .exposedHeaders(
                        HttpHeaders.AUTHORIZATION
                        , HttpHeaders.CONTENT_DISPOSITION
                        , CustomHttpHeaders.REQUEST_ID
                )
                .maxAge(3600);

    }

    /**
     * 配置全局异常处理
     */
    @Bean
    @ConditionalOnMissingBean
    public CommonExceptionHandlerAdvice commonExceptionHandler() {
        return new CommonExceptionHandlerAdvice();
    }


    /**
     * 配置请求上下文过滤器
     */
    @Bean("travisRequestContextFilter")
    public FilterRegistrationBean<RequestContextFilter> requestContextFilter(@Qualifier(
            "handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {
        return createFilterBean(new RequestContextFilter(handlerExceptionResolver),
                WebFilterOrders.REQUEST_CONTEXT_FILTER);
    }

    /**
     * 配置请求ID过滤器
     */
    @Bean
    public FilterRegistrationBean<RequestIdFilter> requestIdFilter(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) {
        return createFilterBean(new RequestIdFilter(handlerExceptionResolver),
                WebFilterOrders.REQUEST_ID_FILTER);
    }

    /**
     * 创建Filter Bean
     */
    private static <T extends Filter> FilterRegistrationBean<T> createFilterBean(T filter, Integer order) {
        FilterRegistrationBean<T> bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(order);
        return bean;
    }

}
