package com.travis.infrastructure.framework.websocket.message;

import com.fasterxml.jackson.annotation.JsonInclude;
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
 *   "loginType": "admin",
 *   "fromUser": "system",
 *   "toUser": "123",
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebSocketMessage {

    /** 消息类型 */
    private WebSocketMessageType type;

    /** 登录类型（如 "admin"、"user"），点对点消息必填，广播消息为 null */
    private String loginType;

    /** 发送者标识 */
    private String fromUser;

    /** 接收者 userId（仅点对点消息） */
    private String toUser;

    /** 消息内容，业务自定义 JSON 字符串或纯文本 */
    private Object content;

    /** 消息时间戳（毫秒） */
    private long timestamp;

    // ==================== 静态工厂方法 ====================

    /**
     * 创建点对点消息
     *
     * @param loginType 接收者的登录类型（如 "admin"）
     * @param fromUser 发送者标识
     * @param toUser 接收者 userId
     * @param content 消息内容
     */
    public static WebSocketMessage toUser(
            String loginType, String fromUser, String toUser, Object content) {
        return WebSocketMessage.builder()
                .type(WebSocketMessageType.USER)
                .loginType(loginType)
                .fromUser(fromUser)
                .toUser(toUser)
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
}
