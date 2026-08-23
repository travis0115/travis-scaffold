package com.travis.monolith.app.user.internal.event;

import lombok.Builder;

/** 客户端用户登录事件。 */
@Builder
public record AppUserLoginEvent(
        String username, int status, String message, String ip, String browser, String os) {}
