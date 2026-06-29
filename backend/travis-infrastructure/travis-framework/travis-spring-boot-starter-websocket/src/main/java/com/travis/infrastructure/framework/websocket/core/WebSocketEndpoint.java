package com.travis.infrastructure.framework.websocket.core;

import java.util.Map;

/** 业务模块声明的 WebSocket 端点。 */
public record WebSocketEndpoint(String path, Map<String, Object> attributes) {

    public WebSocketEndpoint {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
