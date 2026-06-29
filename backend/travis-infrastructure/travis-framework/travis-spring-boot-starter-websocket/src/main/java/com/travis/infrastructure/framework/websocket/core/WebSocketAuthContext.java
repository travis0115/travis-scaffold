package com.travis.infrastructure.framework.websocket.core;

import java.util.Collections;
import java.util.Map;

/** WebSocket 握手认证后的连接主体。 */
public record WebSocketAuthContext(String principal, Map<String, Object> attributes) {

    public WebSocketAuthContext {
        attributes = attributes == null ? Map.of() : Collections.unmodifiableMap(attributes);
    }
}
