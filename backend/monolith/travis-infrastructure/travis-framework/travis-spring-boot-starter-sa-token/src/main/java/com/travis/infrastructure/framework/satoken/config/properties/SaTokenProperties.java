package com.travis.infrastructure.framework.satoken.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 鉴权配置属性
 *
 * @author travis
 */
@Data
@ConfigurationProperties(prefix = "travis.web.security")
public class SaTokenProperties {
    /**
     * 登录类型鉴权规则列表，每条规则定义一种 loginType 的路径拦截策略。
     * starter 会为每条规则注册独立的 SaInterceptor，使用对应的 StpLogic 进行登录校验。
     * <p>
     * 示例配置：
     * <pre>
     * auth-rules:
     *   - login-type: admin
     *     path-patterns:
     *       - /api/admin/**
     *     exclude-path-patterns:
     *       - /api/admin/system/auth/login
     * </pre>
     */
    private List<AuthRule> authRules = new ArrayList<>();

    @Data
    public static class AuthRule {

        /**
         * 登录类型标识，对应 StpLogic.getLoginType() 和 LoginType 枚举的 code
         */
        private String loginType;

        /**
         * 该登录类型需要拦截的路径模式（Ant 风格），如 /api/admin/**
         */
        private List<String> pathPatterns = new ArrayList<>();

        /**
         * 该登录类型下需要排除的路径模式（Ant 风格），如 /api/admin/system/auth/login
         */
        private List<String> excludePathPatterns = new ArrayList<>();
    }
}
