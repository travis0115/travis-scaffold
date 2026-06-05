package com.travis.infrastructure.framework.satoken.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpLogic;
import com.travis.infrastructure.framework.satoken.config.properties.SaTokenProperties;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.infrastructure.framework.satoken.core.UserContextInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置类，根据 YAML 中的 auth-rules 自动创建 {@link StpLogic} 并注册拦截器。
 * <p>
 * 每条 auth-rule 定义一种 loginType 的路径拦截策略，本配置类会：
 * <ul>
 *   <li>通过 {@link StpKit} 为每个 loginType 自动创建 {@link StpLogic} 实例</li>
 *   <li>为每条规则注册独立的 {@link SaInterceptor}，自动路由到对应 StpLogic 校验</li>
 * </ul>
 * 新增 loginType 只需在 YAML 中添加 auth-rule 配置即可，无需手动注册 Bean。
 *
 * @author travis
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(SaTokenProperties.class)
@RequiredArgsConstructor
public class TravisSaTokenAutoConfiguration implements WebMvcConfigurer {

    private final SaTokenProperties saTokenProperties;

    /**
     * 注册拦截器：为每条 auth-rule 注册独立的 SaInterceptor，
     * 拦截器 lambda 中通过 StpKit 路由到对应 StpLogic 校验。
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {

        for (SaTokenProperties.AuthRule rule : saTokenProperties.getAuthRules()) {

            registry.addInterceptor(new SaInterceptor(handle ->
                            StpKit.of(rule.getLoginType()).checkLogin()))
                    .addPathPatterns(rule.getPathPatterns())
                    .excludePathPatterns(rule.getExcludePathPatterns())
                    .excludeHttpMethods(HttpMethod.OPTIONS)
                    .order(Ordered.HIGHEST_PRECEDENCE + 100);

            log.info("[Sa-Token] 注册拦截器: loginType={}, pathPatterns={}, excludePathPatterns={}",
                    rule.getLoginType(), rule.getPathPatterns(), rule.getExcludePathPatterns());
        }

        // 注册用户上下文拦截器
        registry.addInterceptor(new UserContextInterceptor())
                .addPathPatterns("/api/**")
                .order(Ordered.HIGHEST_PRECEDENCE + 200);
    }

    /**
     * Sa-Token 整合 jwt（默认 StpLogic，loginType="login"，供 StpUtil 使用）
     */
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }

    /**
     * StpKit 初始化，根据 YAML 配置自动创建所有 loginType 对应的 StpLogic
     */
    @Bean
    public StpKit stpKit() {
        return new StpKit(saTokenProperties);
    }
}
