package com.travis.monolith.system.internal.exception;

import com.travis.infrastructure.framework.web.core.exception.IErrorCode;
import lombok.AllArgsConstructor;

/**
 * 业务状态码枚举
 * <p>
 * 模块前缀 + 模块内唯一
 * 例如：USER_0001
 */
@AllArgsConstructor
public enum SystemErrorCode implements IErrorCode {

    /**
     * System模块
     */
    SYSTEM_AUTH_LOGIN_BAD_CREDENTIALS("SYS_0001", "用户名或密码错误"),
    SYSTEM_AUTH_LOGIN_USER_DISABLED("SYS_0002", "账号已被禁用"),

    ;

    private final String code;

    private final String msg;


    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getMsg() {
        return this.msg;
    }
}
