package com.travis.infrastructure.framework.websocket.core.auth;

import org.jspecify.annotations.Nullable;

/** WebSocket 认证适配接口，由具体登录框架提供实现。 */
public interface WebSocketAuthService {

    /** 消费握手凭证并返回连接主体，认证失败时返回 null。 */
    @Nullable WebSocketAuthContext authenticate(WebSocketAuthRequest request);

    /** 校验已建立连接是否仍然有效。 */
    boolean isConnectionValid(WebSocketAuthContext context);
}
