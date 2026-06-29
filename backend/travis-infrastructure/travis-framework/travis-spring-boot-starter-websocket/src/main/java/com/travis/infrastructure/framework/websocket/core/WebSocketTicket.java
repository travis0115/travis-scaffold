package com.travis.infrastructure.framework.websocket.core;

import java.io.Serializable;

/** WebSocket 握手 ticket 绑定的登录上下文。 */
public record WebSocketTicket(String loginType, String userId, String token, long createdAt)
        implements Serializable {}
