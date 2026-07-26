package com.travis.infrastructure.framework.websocket.core.sender;

import com.travis.infrastructure.framework.websocket.core.message.WebSocketMessage;
import com.travis.infrastructure.framework.websocket.core.session.WebSocketSessionManager;
import lombok.AllArgsConstructor;

/**
 * WebSocket 消息发送工具类，供业务层直接注入使用。
 *
 * <p>使用示例：
 *
 * <pre>{@code
 * @Service
 * @RequiredArgsConstructor
 * public class NotificationService {
 *
 *     private final WebSocketMessageSender wsSender;
 *
 *     public void notifyAdmin(Long adminId, String msg) {
 *         wsSender.sendToPrincipal("admin:" + adminId,
 *                 WebSocketMessage.toPrincipal(
 *                         WebSocketSender.SYSTEM, "admin:" + adminId, msg));
 *     }
 *
 *     public void pushMarketData(MarketDataVO data) {
 *         wsSender.sendToAll(WebSocketMessage.toAll("market", data));
 *     }
 * }
 * }</pre>
 *
 * @author travis
 */
@AllArgsConstructor
public class WebSocketMessageSender {

    private final WebSocketSessionManager sessionManager;

    /**
     * 发送消息给指定连接主体
     *
     * @param principal 连接主体
     * @param message 消息体
     */
    public void sendToPrincipal(String principal, WebSocketMessage message) {
        sessionManager.sendToPrincipal(principal, message);
    }

    /**
     * 广播消息给所有已连接主体
     *
     * @param message 消息体
     */
    public void sendToAll(WebSocketMessage message) {
        sessionManager.sendToAll(message);
    }

    /**
     * 判断连接主体是否已连接
     *
     * @param principal 连接主体
     * @return 是否已连接
     */
    public boolean isConnected(String principal) {
        return sessionManager.isConnected(principal);
    }
}
