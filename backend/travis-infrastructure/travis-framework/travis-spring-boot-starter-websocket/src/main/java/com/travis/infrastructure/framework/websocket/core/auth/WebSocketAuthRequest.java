package com.travis.infrastructure.framework.websocket.core.auth;

import java.util.Map;
import org.springframework.http.HttpHeaders;

/** WebSocket 握手认证请求。 */
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
