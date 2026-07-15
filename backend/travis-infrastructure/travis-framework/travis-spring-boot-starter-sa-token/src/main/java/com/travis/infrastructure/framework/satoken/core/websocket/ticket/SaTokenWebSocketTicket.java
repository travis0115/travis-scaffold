package com.travis.infrastructure.framework.satoken.core.websocket.ticket;

import java.io.Serializable;
import java.util.Map;

/**
 * Sa-Token WebSocket ticket 绑定的认证上下文。
 *
 * @param principal 连接主体唯一标识
 * @param attributes 认证后写入 Session 的属性
 * @param createdAt 凭证创建时间戳
 */
public record SaTokenWebSocketTicket(
        String principal, Map<String, Object> attributes, long createdAt) implements Serializable {

    public SaTokenWebSocketTicket {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
