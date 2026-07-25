package com.travis.infrastructure.framework.websocket.core.message;

/**
 * WebSocket 业务事件契约。
 *
 * <p>业务模块通过枚举实现此接口，每个枚举值代表一个需要通过 WebSocket 推送的业务事件。
 */
public interface WebSocketEvent {

    /**
     * 获取事件名称。
     *
     * @return 事件名称
     */
    String getEvent();
}
