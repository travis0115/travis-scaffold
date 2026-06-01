package com.travis.infrastructure.framework.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * Web 配置属性
 *
 * @author travis
 */
@Data
@ConfigurationProperties(prefix = "travis.web")
@Validated
public class WebProperties {

    /**
     * API 路径前缀配置列表
     */
    private List<ApiPrefix> apis = new ArrayList<>();

    /**
     * API 路径前缀配置
     */
    @Data
    public static class ApiPrefix {

        /**
         * 路径前缀，例如：/api/admin
         */
        private String prefix;

        /**
         * Controller 包名匹配规则，例如：.controller.admin
         * 支持包含匹配，即 Controller 包名包含此字符串时应用该前缀
         */
        private String packagePattern;

        /**
         * 是否启用该前缀配置
         */
        private boolean enabled = true;
    }
}
