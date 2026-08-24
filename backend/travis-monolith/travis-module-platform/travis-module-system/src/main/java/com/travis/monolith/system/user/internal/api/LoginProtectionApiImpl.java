package com.travis.monolith.system.user.internal.api;

import cn.hutool.crypto.digest.DigestUtil;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.monolith.system.config.api.SysConfigApi;
import com.travis.monolith.system.user.api.LoginProtectionApi;
import com.travis.monolith.system.user.internal.service.LoginProtectionStore;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** 基于 Redis TTL 计数的登录失败保护实现。 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LoginProtectionApiImpl implements LoginProtectionApi {

    private static final String KEY_PREFIX = "security:login:";
    private static final int MIN_WINDOW_SECONDS = 60;
    private static final int MAX_WINDOW_SECONDS = 3600;
    private static final int MIN_ACCOUNT_FAILURES = 3;
    private static final int MAX_ACCOUNT_FAILURES = 20;
    private static final int MIN_IP_FAILURES = 10;
    private static final int MAX_IP_FAILURES = 5000;
    private static final int MIN_LOCK_SECONDS = 60;
    private static final int MAX_LOCK_SECONDS = 86400;

    private final SysConfigApi configApi;
    private final LoginProtectionStore store;

    @Override
    public void checkAllowed(String loginType, String username, String clientIp) {
        var settings = loadSettings(loginType);
        if (!settings.enabled()) {
            return;
        }
        var keys = keys(loginType, username, clientIp);
        try {
            if (store.isLocked(keys.accountLock()) || store.isLocked(keys.ipLock())) {
                throw tooManyRequests();
            }
        } catch (BizException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.error("登录保护 Redis 检查失败，已降级为边缘限流, loginType={}", loginType, exception);
        }
    }

    @Override
    public void recordFailure(String loginType, String username, String clientIp) {
        var settings = loadSettings(loginType);
        if (!settings.enabled()) {
            return;
        }
        var keys = keys(loginType, username, clientIp);
        try {
            var windowMillis = settings.windowSeconds() * 1000L;
            var accountFailures = store.increment(keys.accountFailure(), windowMillis);
            var ipFailures = store.increment(keys.ipFailure(), windowMillis);
            var locked = false;
            if (accountFailures >= settings.accountMaxFailures()) {
                store.lock(keys.accountLock(), settings.accountLockSeconds() * 1000L);
                store.delete(keys.accountFailure());
                locked = true;
                log.warn(
                        "登录账号维度触发临时锁, loginType={}, digest={}",
                        loginType,
                        keys.accountDigestPrefix());
            }
            if (ipFailures >= settings.ipMaxFailures()) {
                store.lock(keys.ipLock(), settings.ipLockSeconds() * 1000L);
                store.delete(keys.ipFailure());
                locked = true;
                log.warn(
                        "登录 IP 维度触发临时锁, loginType={}, digest={}", loginType, keys.ipDigestPrefix());
            }
            if (locked) {
                throw tooManyRequests();
            }
        } catch (BizException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.error("登录保护 Redis 计数失败，已降级为边缘限流, loginType={}", loginType, exception);
        }
    }

    @Override
    public void recordSuccess(String loginType, String username) {
        var settings = loadSettings(loginType);
        if (!settings.enabled()) {
            return;
        }
        var accountDigest = digest(normalizeUsername(username));
        try {
            store.delete(key(loginType, "failure:account", accountDigest));
        } catch (RuntimeException exception) {
            log.error("登录保护 Redis 清理失败, loginType={}", loginType, exception);
        }
    }

    private LoginProtectionSettings loadSettings(String loginType) {
        var defaults =
                LoginType.APP.equals(loginType)
                        ? new LoginProtectionSettings(true, 600, 8, 600, 300, 600)
                        : new LoginProtectionSettings(true, 600, 5, 900, 60, 900);
        var prefix = "security.login." + normalizeLoginType(loginType) + ".";
        try {
            return new LoginProtectionSettings(
                    readBoolean(prefix + "enabled", defaults.enabled()),
                    readInt(
                            prefix + "window-seconds",
                            defaults.windowSeconds(),
                            MIN_WINDOW_SECONDS,
                            MAX_WINDOW_SECONDS),
                    readInt(
                            prefix + "account-max-failures",
                            defaults.accountMaxFailures(),
                            MIN_ACCOUNT_FAILURES,
                            MAX_ACCOUNT_FAILURES),
                    readInt(
                            prefix + "account-lock-seconds",
                            defaults.accountLockSeconds(),
                            MIN_LOCK_SECONDS,
                            MAX_LOCK_SECONDS),
                    readInt(
                            prefix + "ip-max-failures",
                            defaults.ipMaxFailures(),
                            MIN_IP_FAILURES,
                            MAX_IP_FAILURES),
                    readInt(
                            prefix + "ip-lock-seconds",
                            defaults.ipLockSeconds(),
                            MIN_LOCK_SECONDS,
                            MAX_LOCK_SECONDS));
        } catch (RuntimeException exception) {
            log.warn("登录保护动态配置读取失败，使用安全默认值, loginType={}", loginType, exception);
            return defaults;
        }
    }

    private boolean readBoolean(String key, boolean defaultValue) {
        var value = configApi.getValue(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }
        log.warn("登录保护配置不是布尔值，使用默认值, configKey={}", key);
        return defaultValue;
    }

    private int readInt(String key, int defaultValue, int min, int max) {
        var value = configApi.getValue(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            var parsed = Integer.parseInt(value);
            if (parsed >= min && parsed <= max) {
                return parsed;
            }
        } catch (NumberFormatException ignored) {
            // 统一在下方记录不合法配置。
        }
        log.warn("登录保护配置越界或格式错误，使用默认值, configKey={}", key);
        return defaultValue;
    }

    private LoginProtectionKeys keys(String loginType, String username, String clientIp) {
        var accountDigest = digest(normalizeUsername(username));
        var ipDigest = digest(clientIp == null ? "unknown" : clientIp.trim());
        return new LoginProtectionKeys(
                key(loginType, "failure:account", accountDigest),
                key(loginType, "failure:ip", ipDigest),
                key(loginType, "lock:account", accountDigest),
                key(loginType, "lock:ip", ipDigest),
                accountDigest.substring(0, 8),
                ipDigest.substring(0, 8));
    }

    private String key(String loginType, String dimension, String digest) {
        return KEY_PREFIX + normalizeLoginType(loginType) + ":" + dimension + ":" + digest;
    }

    private String normalizeLoginType(String loginType) {
        var normalized = loginType == null ? "unknown" : loginType.trim().toLowerCase(Locale.ROOT);
        return normalized.matches("[a-z0-9_-]{1,32}") ? normalized : "unknown";
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    private String digest(String value) {
        return DigestUtil.sha256Hex(value);
    }

    private BizException tooManyRequests() {
        return new BizException(CommonErrorCode.TOO_MANY_REQUESTS);
    }

    private record LoginProtectionSettings(
            boolean enabled,
            int windowSeconds,
            int accountMaxFailures,
            int accountLockSeconds,
            int ipMaxFailures,
            int ipLockSeconds) {}

    private record LoginProtectionKeys(
            String accountFailure,
            String ipFailure,
            String accountLock,
            String ipLock,
            String accountDigestPrefix,
            String ipDigestPrefix) {}
}
