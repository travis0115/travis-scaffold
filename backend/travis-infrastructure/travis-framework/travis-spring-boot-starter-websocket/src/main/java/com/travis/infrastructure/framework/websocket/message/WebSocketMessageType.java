package com.travis.infrastructure.framework.websocket.message;

/**
 * WebSocket 消息类型枚举
 *
 * @author travis
 */
public enum WebSocketMessageType {

    /** 服务端心跳 */
    PING,

    /** 客户端心跳回复 */
    PONG,

    /** 点对点消息 */
    USER,

    /** 广播消息 */
    BROADCAST
}
