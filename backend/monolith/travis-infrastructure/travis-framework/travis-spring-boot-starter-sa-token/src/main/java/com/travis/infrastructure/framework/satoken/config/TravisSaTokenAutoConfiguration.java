package com.travis.infrastructure.framework.satoken.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import com.travis.infrastructure.framework.satoken.config.properties.SaTokenProperties;
import com.travis.infrastructure.framework.satoken.core.interceptor.UserContextInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Sa-Token 配置类
 *
 * @author travis
 */
@AutoConfiguration
@EnableConfigurationProperties(SaTokenProperties.class)
@RequiredArgsConstructor
public class TravisSaTokenAutoConfiguration implements WebMvcConfigurer {

    private final SaTokenProperties saTokenProperties;

    /**
     * 注册拦截器
     *
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验。
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/api/**")
                .excludeHttpMethods(HttpMethod.OPTIONS)
                .excludePathPatterns(excludePaths())
                .order(Ordered.HIGHEST_PRECEDENCE + 100);
        registry.addInterceptor(new UserContextInterceptor())
                .addPathPatterns("/api/**")
                .order(Ordered.HIGHEST_PRECEDENCE + 200);
    }

    /**
     * 动态获取哪些 path 可以忽略鉴权
     */
    public List<String> excludePaths() {
        return saTokenProperties.getPermitPaths();
    }

    /**
     * Sa-Token 整合 jwt
     */
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }

}
