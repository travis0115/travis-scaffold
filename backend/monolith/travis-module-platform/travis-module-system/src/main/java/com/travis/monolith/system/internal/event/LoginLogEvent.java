package com.travis.monolith.system.internal.event;

import lombok.Getter;

/**
 * 登录日志事件，记录登录的用户名、状态和提示信息
 *
 * @author travis
 */
@Getter
public class LoginLogEvent {

    /**
     * 登录用户名
     */
    private final String username;
    /**
     * 登录状态（0-失败 1-成功）
     */
    private final int status;
    /**
     * 提示信息
     */
    private final String message;

    public LoginLogEvent(String username, int status, String message) {
        this.username = username;
        this.status = status;
        this.message = message;
    }
}
