package com.travis.infrastructure.framework.websocket.core.endpoint;

import java.util.Map;

/**
 * 业务模块声明的 WebSocket 端点。
 *
 * @param path 端点握手路径
 * @param attributes 端点附加属性
 */
public record WebSocketEndpoint(String path, Map<String, Object> attributes) {

    public WebSocketEndpoint {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
