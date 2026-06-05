package com.travis.infrastructure.framework.web.config;

import com.travis.infrastructure.common.web.constant.WebFilterOrders;
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
public class TravisLoggingAutoConfiguration implements WebMvcConfigurer {

    /** 注册 MDC 脱敏规则。 按需为每个需要脱敏的 MDC key 绑定规则。 */
    @PostConstruct
    public void postConstruct() {
        // 示例1：如果 trace_id 实际上是手机号等敏感数据，按 MobileDesensitize 默认规则脱敏
        //        DesensitizeMdcJsonProvider.registerRule(MdcKeys.TRACE_ID,
        //                DesensitizeUtils.resolveDefaultRule(MobileDesensitize.class)::apply);

        // 示例2：直接用 SliderRule，保留前2后2
        //        DesensitizeMdcJsonProvider.registerRule(MdcKeys.TRACE_ID,
        //                new SliderDesensitizeRule(2, 2, '*')::apply);

    }

    /** 配置 Access Log 过滤器 */
    @Bean
    public FilterRegistrationBean<AccessLogFilter> accessLogFilter(
            @Qualifier("handlerExceptionResolver")
                    HandlerExceptionResolver handlerExceptionResolver,
            @Qualifier("requestMappingHandlerMapping")
                    RequestMappingHandlerMapping requestMappingHandlerMapping) {
        FilterRegistrationBean<AccessLogFilter> bean =
                new FilterRegistrationBean<>(
                        new AccessLogFilter(
                                handlerExceptionResolver, requestMappingHandlerMapping));
        bean.setOrder(WebFilterOrders.ACCESS_LOG_FILTER);
        return bean;
    }
}
