package com.travis.infrastructure.framework.xxljob.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** XXL-JOB 执行器与管理端入口配置。 */
@ConfigurationProperties(prefix = "travis.xxl-job")
public record TravisXxlJobProperties(Admin admin, Executor executor, Sso sso) {

    public TravisXxlJobProperties {
        admin = admin == null ? new Admin("", "", "", 3) : admin;
        executor =
                executor == null
                        ? new Executor(false, "travis-executor", "", "", 9999, "", 30, "")
                        : executor;
        sso = sso == null ? new Sso(false, "", 60) : sso;
    }

    public record Admin(String addresses, String webUrl, String accessToken, int timeout) {}

    public record Executor(
            boolean enabled,
            String appName,
            String address,
            String ip,
            int port,
            String logPath,
            int logRetentionDays,
            String excludedPackage) {}

    public record Sso(boolean enabled, String secret, long ttlSeconds) {}
}
