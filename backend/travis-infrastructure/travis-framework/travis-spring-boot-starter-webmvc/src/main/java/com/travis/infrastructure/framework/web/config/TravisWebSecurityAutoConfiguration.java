package com.travis.infrastructure.framework.web.config;

import com.travis.infrastructure.framework.web.config.properties.WebProperties;
import com.travis.infrastructure.framework.web.core.aop.NoRepeatSubmitAspect;
import com.travis.infrastructure.framework.web.core.xss.HtmlSanitizer;
import com.travis.infrastructure.framework.web.core.xss.JsoupHtmlSanitizer;
import com.travis.infrastructure.framework.web.core.xss.SanitizeHtmlJacksonModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import tools.jackson.databind.JacksonModule;

/** Web 输入安全与防重复提交自动配置。 */
@AutoConfiguration(
        afterName = {
            "org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration",
            "com.travis.infrastructure.framework.redis.config.TravisRedisAutoConfiguration"
        })
@EnableConfigurationProperties(WebProperties.class)
public class TravisWebSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public HtmlSanitizer htmlSanitizer() {
        return new JsoupHtmlSanitizer();
    }

    @Bean
    public JacksonModule sanitizeHtmlJacksonModule(HtmlSanitizer htmlSanitizer) {
        return new SanitizeHtmlJacksonModule(htmlSanitizer);
    }

    @Bean
    @ConditionalOnClass(StringRedisTemplate.class)
    @ConditionalOnBean(StringRedisTemplate.class)
    public NoRepeatSubmitAspect noRepeatSubmitAspect(
            StringRedisTemplate redisTemplate,
            WebProperties webProperties,
            Environment environment) {
        return new NoRepeatSubmitAspect(
                redisTemplate,
                buildRedisKeyPrefix(
                        environment.getProperty("travis.redis.key-prefix"),
                        webProperties.getNoRepeatSubmit().getKeyPrefix()));
    }

    private String buildRedisKeyPrefix(String redisKeyPrefix, String keyPrefix) {
        return normalizePrefix(redisKeyPrefix) + (keyPrefix == null ? "" : keyPrefix);
    }

    private String normalizePrefix(String prefix) {
        if (!StringUtils.hasText(prefix)) {
            return "";
        }
        return prefix.endsWith(":") ? prefix : prefix + ":";
    }
}
