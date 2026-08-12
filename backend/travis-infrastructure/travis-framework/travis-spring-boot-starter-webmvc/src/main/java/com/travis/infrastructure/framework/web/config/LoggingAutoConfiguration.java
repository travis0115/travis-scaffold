package com.travis.infrastructure.framework.web.config;

import com.travis.infrastructure.common.web.constant.WebFilterOrder;
import com.travis.infrastructure.framework.desensitize.core.Desensitizer;
import com.travis.infrastructure.framework.web.core.filter.AccessLogFilter;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * 日志相关自动配置类
 *
 * @author travis
 */
@AutoConfiguration
@Slf4j
public class LoggingAutoConfiguration implements WebMvcConfigurer {

    /** 注册 MDC 脱敏规则。 按需为每个需要脱敏的 MDC key 绑定规则。 */
    @PostConstruct
    public void postConstruct() {
        // 示例：直接用 SliderRule，保留前2后2
        //        DesensitizeMdcJsonProvider.registerRule(MdcKeys.TRACE_ID,
        //                new SliderDesensitizeRule(2, 2, '*')::apply);

    }

    /** 配置 Access Log 过滤器 */
    @Bean
    public FilterRegistrationBean<AccessLogFilter> accessLogFilter(
            @Qualifier("handlerExceptionResolver")
                    HandlerExceptionResolver handlerExceptionResolver,
            @Qualifier("requestMappingHandlerMapping")
                    RequestMappingHandlerMapping requestMappingHandlerMapping,
            Desensitizer desensitizer) {
        FilterRegistrationBean<AccessLogFilter> bean =
                new FilterRegistrationBean<>(
                        new AccessLogFilter(
                                handlerExceptionResolver,
                                requestMappingHandlerMapping,
                                desensitizer));
        bean.setOrder(WebFilterOrder.ACCESS_LOG);
        return bean;
    }
}
