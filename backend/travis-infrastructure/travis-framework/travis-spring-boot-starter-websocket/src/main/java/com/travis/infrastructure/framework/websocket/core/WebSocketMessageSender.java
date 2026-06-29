package com.travis.infrastructure.framework.websocket.core;

import com.travis.infrastructure.framework.websocket.message.WebSocketMessage;

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
 *                 WebSocketMessage.toPrincipal("system", "admin:" + adminId, msg));
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
public class WebSocketMessageSender {

    private final WebSocketSessionManager sessionManager;

    public WebSocketMessageSender(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

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
     * 广播消息给所有在线用户
     *
     * @param message 消息体
     */
    public void sendToAll(WebSocketMessage message) {
        sessionManager.sendToAll(message);
    }

    /**
     * 判断连接主体是否在线
     *
     * @param principal 连接主体
     * @return 是否在线
     */
    public boolean isOnline(String principal) {
        return sessionManager.isOnline(principal);
    }
}
