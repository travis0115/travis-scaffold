package com.travis.infrastructure.framework.websocket.core.auth;

import java.util.Map;
import org.springframework.http.HttpHeaders;

/**
 * WebSocket 握手认证请求。
 *
 * @param path 握手路径
 * @param credential 握手凭证
 * @param headers 握手请求头
 * @param remoteAddr 远端地址
 * @param attributes 端点附加属性
 */
public record WebSocketAuthRequest(
        String path,
        String credential,
        HttpHeaders headers,
        String remoteAddr,
        Map<String, Object> attributes) {

    public WebSocketAuthRequest {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
