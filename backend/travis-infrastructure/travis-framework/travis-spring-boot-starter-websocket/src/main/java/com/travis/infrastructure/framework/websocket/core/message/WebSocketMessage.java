package com.travis.infrastructure.framework.websocket.core.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket 消息模型，所有 WebSocket 通信统一使用此格式。
 *
 * <p>前端收到的 JSON 格式示例：
 *
 * <pre>{@code
 * {
 *   "type": "USER",
 *   "fromUser": "system",
 *   "to": "admin:123",
 *   "content": "订单已创建",
 *   "timestamp": 1718000000000
 * }
 * }</pre>
 *
 * @author travis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {

    /** 消息类型 */
    private WebSocketMessageType type;

    /** 发送者标识 */
    private String fromUser;

    /** 接收者连接主体（仅点对点消息） */
    private String to;

    /** 消息内容，业务自定义 JSON 字符串或纯文本 */
    private Object content;

    /** 消息时间戳（毫秒） */
    private long timestamp;

    /** 来源实例 ID，仅用于集群 Pub/Sub 去重 */
    private String sourceInstanceId;

    // ==================== 静态工厂方法 ====================

    /**
     * 创建点对点消息
     *
     * @param fromUser 发送者标识
     * @param to 接收者连接主体
     * @param content 消息内容
     */
    public static WebSocketMessage toPrincipal(String fromUser, String to, Object content) {
        return WebSocketMessage.builder()
                .type(WebSocketMessageType.USER)
                .fromUser(fromUser)
                .to(to)
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /** 创建广播消息 */
    public static WebSocketMessage toAll(String fromUser, Object content) {
        return WebSocketMessage.builder()
                .type(WebSocketMessageType.BROADCAST)
                .fromUser(fromUser)
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /** 创建心跳消息 */
    public static WebSocketMessage ping() {
        return WebSocketMessage.builder()
                .type(WebSocketMessageType.PING)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /** 创建关闭指定连接主体的内部消息 */
    public static WebSocketMessage close(String principal) {
        return WebSocketMessage.builder()
                .type(WebSocketMessageType.CLOSE)
                .to(principal)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
