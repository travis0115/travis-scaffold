package com.travis.infrastructure.framework.websocket.core.message;

import java.util.LinkedHashMap;
import java.util.Map;
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

    /**
     * 创建点对点业务事件消息。
     *
     * @param fromUser 发送者标识
     * @param to 接收者连接主体
     * @param event 业务事件
     * @param content 事件内容
     */
    public static WebSocketMessage toPrincipal(
            String fromUser, String to, WebSocketEvent event, Map<String, Object> content) {
        return toPrincipal(fromUser, to, eventContent(event, content));
    }

    /**
     * 创建无附加内容的点对点业务事件消息。
     *
     * @param fromUser 发送者标识
     * @param to 接收者连接主体
     * @param event 业务事件
     */
    public static WebSocketMessage toPrincipal(String fromUser, String to, WebSocketEvent event) {
        return toPrincipal(fromUser, to, event, Map.of());
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

    /**
     * 创建业务事件广播消息。
     *
     * @param fromUser 发送者标识
     * @param event 业务事件
     * @param content 事件内容
     */
    public static WebSocketMessage toAll(
            String fromUser, WebSocketEvent event, Map<String, Object> content) {
        return toAll(fromUser, eventContent(event, content));
    }

    /**
     * 创建无附加内容的业务事件广播消息。
     *
     * @param fromUser 发送者标识
     * @param event 业务事件
     */
    public static WebSocketMessage toAll(String fromUser, WebSocketEvent event) {
        return toAll(fromUser, event, Map.of());
    }

    /** 创建命名空间广播消息。 */
    public static WebSocketMessage toNamespace(
            String fromUser, String namespace, WebSocketEvent event, Map<String, Object> content) {
        return WebSocketMessage.builder()
                .type(WebSocketMessageType.NAMESPACE)
                .fromUser(fromUser)
                .to(namespace)
                .content(eventContent(event, content))
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

    /** 创建关闭指定连接主体下匹配属性连接的内部消息 */
    public static WebSocketMessage close(
            String principal, String attributeName, Object attributeValue) {
        return WebSocketMessage.builder()
                .type(WebSocketMessageType.CLOSE)
                .to(principal)
                .content(Map.of("attributeName", attributeName, "attributeValue", attributeValue))
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /** 创建立即关闭指定连接主体的内部消息 */
    public static WebSocketMessage closeImmediately(String principal) {
        return WebSocketMessage.builder()
                .type(WebSocketMessageType.CLOSE_IMMEDIATELY)
                .to(principal)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /** 组装统一的业务事件内容。 */
    private static Map<String, Object> eventContent(
            WebSocketEvent event, Map<String, Object> content) {
        Map<String, Object> eventContent = new LinkedHashMap<>(content);
        eventContent.put("event", event.getEvent());
        return eventContent;
    }
}
