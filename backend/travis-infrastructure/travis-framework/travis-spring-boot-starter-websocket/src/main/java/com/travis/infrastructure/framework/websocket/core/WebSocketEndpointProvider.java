package com.travis.infrastructure.framework.websocket.core;

import java.util.List;

/** WebSocket 端点提供者，用于按配置动态声明多个端点。 */
public interface WebSocketEndpointProvider {

    List<WebSocketEndpoint> getEndpoints();
}
