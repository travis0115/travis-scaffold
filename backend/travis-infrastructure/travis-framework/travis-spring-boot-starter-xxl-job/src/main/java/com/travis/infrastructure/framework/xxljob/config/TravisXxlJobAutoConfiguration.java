package com.travis.infrastructure.framework.xxljob.config;

import com.travis.infrastructure.framework.xxljob.config.properties.TravisXxlJobProperties;
import com.travis.infrastructure.framework.xxljob.core.XxlJobSsoUrlService;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** XXL-JOB 自动配置。 */
@AutoConfiguration
@EnableConfigurationProperties(TravisXxlJobProperties.class)
public class TravisXxlJobAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public XxlJobSpringExecutor xxlJobExecutor(TravisXxlJobProperties properties) {
        var admin = properties.admin();
        var executor = properties.executor();
        var bean = new XxlJobSpringExecutor();
        bean.setAdminAddresses(admin.addresses());
        bean.setAccessToken(admin.accessToken());
        bean.setTimeout(admin.timeout());
        bean.setEnabled(executor.enabled());
        bean.setAppname(executor.appName());
        bean.setAddress(executor.address());
        bean.setIp(executor.ip());
        bean.setPort(executor.port());
        bean.setLogPath(executor.logPath());
        bean.setLogRetentionDays(executor.logRetentionDays());
        bean.setExcludedPackage(executor.excludedPackage());
        return bean;
    }

    @Bean
    @ConditionalOnMissingBean
    public XxlJobSsoUrlService xxlJobSsoUrlService(TravisXxlJobProperties properties) {
        return new XxlJobSsoUrlService(properties);
    }
}
