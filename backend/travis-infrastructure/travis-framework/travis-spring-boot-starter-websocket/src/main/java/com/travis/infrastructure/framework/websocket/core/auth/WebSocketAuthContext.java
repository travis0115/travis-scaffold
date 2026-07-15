package com.travis.infrastructure.framework.websocket.core.auth;

import java.util.Collections;
import java.util.Map;

/**
 * WebSocket 握手认证后的连接主体。
 *
 * @param principal 连接主体唯一标识
 * @param attributes 认证后写入 Session 的属性
 */
public record WebSocketAuthContext(String principal, Map<String, Object> attributes) {

    public WebSocketAuthContext {
        attributes = attributes == null ? Map.of() : Collections.unmodifiableMap(attributes);
    }
}
