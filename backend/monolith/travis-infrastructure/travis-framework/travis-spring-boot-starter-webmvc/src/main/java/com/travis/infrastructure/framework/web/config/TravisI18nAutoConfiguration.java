package com.travis.infrastructure.framework.web.config;

import cn.hutool.core.util.StrUtil;
import com.travis.infrastructure.framework.web.core.service.I18nService;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.context.MessageSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.CollectionFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * 自动装配i18n相关组件
 *
 * @author travis
 */
@AutoConfiguration
@AutoConfigureBefore(MessageSourceAutoConfiguration.class)
@EnableConfigurationProperties({MessageSourceProperties.class})
@Import(I18nService.class)
public class TravisI18nAutoConfiguration {


    /**
     * 自定义LocaleResolver，优先使用请求头中的locale信息，如果不存在则使用默认locale
     */
    @Bean
    public LocaleResolver localeResolver(@Value("${spring.web.locale:zh_CN}") Locale locale) {
        return new AcceptHeaderLocaleResolver() {
            @Override
            @NullMarked
            public Locale resolveLocale(HttpServletRequest request) {
                String header = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
                if (StrUtil.isBlank(header)) {
                    return locale;
                }
                return super.resolveLocale(request);
            }
        };
    }


    /**
     * 自定义国际化资源文件
     *
     * @param properties 国际化属性配置
     * @return 自定义国际化资源文件
     */
    @Bean
    public MessageSource messageSource(MessageSourceProperties properties,
                                       @Value("${spring.web.locale:zh_CN}") Locale locale) {
        var messageSource = new ResourceBundleMessageSource();
        // 按照查找顺序设置basenames
        // 1.用户项目的国际化文件（高优先级）
        messageSource.addBasenames(properties.getBasename().toArray(new String[0]));

        // 2. starter的国际化文件（低优先级，作为fallback）
        messageSource.addBasenames("i18n/travis_messages");

        messageSource.setDefaultLocale(locale);
        messageSource.setDefaultEncoding(properties.getEncoding().name());
        messageSource.setFallbackToSystemLocale(properties.isFallbackToSystemLocale());
        Duration cacheDuration = properties.getCacheDuration();
        if (cacheDuration != null) {
            messageSource.setCacheMillis(cacheDuration.toMillis());
        }
        messageSource.setAlwaysUseMessageFormat(properties.isAlwaysUseMessageFormat());
        messageSource.setUseCodeAsDefaultMessage(properties.isUseCodeAsDefaultMessage());
        messageSource.setCommonMessages(loadCommonMessages(properties.getCommonMessages()));
        return messageSource;
    }

    /**
     * 加载公共资源文件中的消息属性
     *
     * @param resources 资源文件列表，可以为null或空列表
     * @return 包含加载的消息属性的Properties对象，如果输入资源列表为空则返回null
     * @throws UncheckedIOException 当加载资源文件失败时抛出
     */
    private @Nullable Properties loadCommonMessages(@Nullable List<Resource> resources) {
        if (CollectionUtils.isEmpty(resources)) {
            return null;
        }
        Properties properties = CollectionFactory.createSortedProperties(false);
        for (Resource resource : resources) {
            try {
                PropertiesLoaderUtils.fillProperties(properties, resource);
            } catch (IOException ex) {
                throw new UncheckedIOException("Failed to load common messages from '%s'".formatted(resource), ex);
            }
        }
        return properties;
    }

    /**
     * 自动装配I18nService
     */
    @Bean
    @ConditionalOnMissingBean
    public I18nService i18nService(MessageSource messageSource) {
        return new I18nService(messageSource);
    }


}
