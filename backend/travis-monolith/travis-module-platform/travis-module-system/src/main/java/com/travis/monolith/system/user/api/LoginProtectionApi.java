package com.travis.monolith.system.user.api;

import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

/** 登录失败保护公开 API，供不同登录端复用账号和 IP 临时锁定能力。 */
@Validated
public interface LoginProtectionApi {

    /** 在查询账号和校验密码前检查当前登录请求是否允许继续。 */
    void checkAllowed(
            @NotBlank String loginType, @NotBlank String username, @NotBlank String clientIp);

    /** 记录一次账号或密码认证失败，达到阈值时抛出限流异常。 */
    void recordFailure(
            @NotBlank String loginType, @NotBlank String username, @NotBlank String clientIp);

    /** 登录成功后清理账号维度失败计数，不清理 IP 维度计数。 */
    void recordSuccess(@NotBlank String loginType, @NotBlank String username);
}
