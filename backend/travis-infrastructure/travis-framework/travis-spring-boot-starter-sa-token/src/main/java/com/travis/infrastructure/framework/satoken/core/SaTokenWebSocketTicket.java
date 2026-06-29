package com.travis.infrastructure.framework.satoken.core;

import java.io.Serializable;
import java.util.Map;

/** Sa-Token WebSocket ticket 绑定的认证上下文。 */
public record SaTokenWebSocketTicket(
        String principal, Map<String, Object> attributes, long createdAt) implements Serializable {

    public SaTokenWebSocketTicket {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
