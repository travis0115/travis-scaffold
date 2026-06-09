package com.travis.monolith.system.user.api.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * 用户登录事件，由认证服务在登录成功或失败时发布
 *
 * @author travis
 */
@Getter
public class UserLoginEvent {

    /** 登录用户名 */
    private final String username;

    /** 登录状态（0-失败 1-成功） */
    private final int status;

    /** 提示信息 */
    private final String message;

    @JsonCreator
    public UserLoginEvent(
            @JsonProperty("username") String username,
            @JsonProperty("status") int status,
            @JsonProperty("message") String message) {
        this.username = username;
        this.status = status;
        this.message = message;
    }
}
