package com.travis.infrastructure.framework.web.config;

import com.travis.infrastructure.common.web.constant.CustomHttpHeader;
import com.travis.infrastructure.common.web.constant.WebFilterOrder;
import com.travis.infrastructure.framework.web.config.properties.WebProperties;
import com.travis.infrastructure.framework.web.core.advice.ApiResponseBodyAdvice;
import com.travis.infrastructure.framework.web.core.advice.I18nResponseBodyAdvice;
import com.travis.infrastructure.framework.web.core.exception.handler.BizExceptionHandler;
import com.travis.infrastructure.framework.web.core.exception.handler.ServerExceptionHandler;
import com.travis.infrastructure.framework.web.core.exception.handler.ValidationExceptionHandler;
import com.travis.infrastructure.framework.web.core.filter.MdcFilter;
import com.travis.infrastructure.framework.web.core.filter.RequestContextFilter;
import jakarta.servlet.Filter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 自动配置类
 *
 * @author travis
 */
@AutoConfiguration
@EnableConfigurationProperties(WebProperties.class)
@RequiredArgsConstructor
public class TravisWebMvcAutoConfiguration implements WebMvcConfigurer {

    private final WebProperties webProperties;

    //    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver handlerExceptionResolver;

    /**
     * 根据Controller所在包名自动添加路径前缀： controller.admin 包 → /api/admin controller.app 包 → /api/app 其他包不加前缀
     */
    @Override
    public void configurePathMatch(@NonNull PathMatchConfigurer configurer) {
        webProperties
                .getApis()
                .forEach(
                        api -> {
                            if (api.isEnabled()) {
                                configurer.addPathPrefix(
                                        api.getPrefix(), clazz -> matchController(clazz, api));
                            }
                        });
    }

    /**
     * 判断 Controller 是否匹配指定的前缀配置
     *
     * @param controllerClass Controller 类
     * @param api API 前缀配置
     * @return 是否匹配
     */
    private boolean matchController(Class<?> controllerClass, WebProperties.ApiPrefix api) {
        String packageName = controllerClass.getPackageName();
        String packagePattern = api.getPackagePattern();

        return packageName.contains(packagePattern);
    }

    /** 跨域处理 若使用 Spring Security,开启 http.cors(withDefaults())，会自动启用该配置 */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true)
                .exposedHeaders(
                        HttpHeaders.AUTHORIZATION,
                        HttpHeaders.CONTENT_DISPOSITION,
                        CustomHttpHeader.REQUEST_ID)
                .maxAge(3600);
    }

    /** 配置统一响应结果 */
    @Bean
    @ConditionalOnMissingBean(I18nResponseBodyAdvice.class)
    public ApiResponseBodyAdvice apiResponseBodyAdvice() {
        return new ApiResponseBodyAdvice();
    }

    /** 配置全局业务异常处理器 */
    @Bean
    @ConditionalOnMissingBean
    public BizExceptionHandler bizExceptionHandler() {
        return new BizExceptionHandler();
    }


    /** 配置全局服务端异常处理器 */
    @Bean
    @ConditionalOnMissingBean
    public ServerExceptionHandler serverExceptionHandler() {
        return new ServerExceptionHandler();
    }

    /** 配置全局参数校验异常处理器 */
    @Bean
    @ConditionalOnMissingBean
    public ValidationExceptionHandler validationExceptionHandler() {
        return new ValidationExceptionHandler();
    }

    /** 配置请求上下文过滤器 */
    @Bean("travisRequestContextFilter")
    public FilterRegistrationBean<RequestContextFilter> requestContextFilter() {
        return createFilterBean(
                new RequestContextFilter(handlerExceptionResolver),
                WebFilterOrder.REQUEST_CONTEXT_FILTER);
    }

    /** 配置MDC过滤器 */
    @Bean
    public FilterRegistrationBean<MdcFilter> requestIdFilter() {
        return createFilterBean(
                new MdcFilter(handlerExceptionResolver), WebFilterOrder.MDC_FILTER);
    }

    /** 创建Filter Bean */
    private static <T extends Filter> FilterRegistrationBean<T> createFilterBean(
            T filter, Integer order) {
        FilterRegistrationBean<T> bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(order);
        return bean;
    }
}
