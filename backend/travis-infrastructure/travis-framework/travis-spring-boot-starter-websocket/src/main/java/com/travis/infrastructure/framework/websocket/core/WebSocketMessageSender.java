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
 *         wsSender.sendToUser(LoginType.ADMIN.getCode(), String.valueOf(adminId),
 *                 WebSocketMessage.toUser("system", String.valueOf(adminId), msg));
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
     * 发送消息给指定用户
     *
     * @param loginType 登录类型（如 "admin"、"user"），对应 {@link
     *     com.travis.infrastructure.common.web.enums.LoginType}
     * @param userId 目标用户 ID
     * @param message 消息体
     */
    public void sendToUser(String loginType, String userId, WebSocketMessage message) {
        sessionManager.sendToUser(loginType, userId, message);
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
     * 判断用户是否在线
     *
     * @param loginType 登录类型
     * @param userId 用户 ID
     * @return 是否在线
     */
    public boolean isOnline(String loginType, String userId) {
        return sessionManager.isOnline(loginType, userId);
    }
}
