package com.travis.infrastructure.framework.websocket.core;

/**
 * WebSocket 会话生命周期监听器 SPI，供业务模块实现。
 *
 * <p>当用户的首个 WebSocket 连接建立或最后一个连接断开时，starter 自动调用所有注册的实现。 多实例部署下，回调仅在用户实际连接的实例上触发。
 *
 * <p>典型用法：业务模块实现此接口，在回调中发布 RocketMQ 事件实现集群范围的上下线通知。
 *
 * <pre>{@code
 * @Component
 * public class OnlineStatusListener implements WebSocketSessionListener {
 *
 *     private final MessagePublisher messagePublisher;
 *
 *     @Override
 *     public void onConnect(String loginType, String userId) {
 *         messagePublisher.publish(SystemEvent.USER_ONLINE,
 *                 new UserOnlinePayload(loginType, userId, true));
 *     }
 *
 *     @Override
 *     public void onDisconnect(String loginType, String userId) {
 *         messagePublisher.publish(SystemEvent.USER_ONLINE,
 *                 new UserOnlinePayload(loginType, userId, false));
 *     }
 * }
 * }</pre>
 *
 * @author travis
 */
public interface WebSocketSessionListener {

    /**
     * 用户首个 WebSocket 连接建立时触发（从 0 → 1）
     *
     * @param loginType 登录类型（如 "admin"、"user"）
     * @param userId 用户 ID
     */
    void onConnect(String loginType, String userId);

    /**
     * 用户最后一个 WebSocket 连接断开时触发（从 1 → 0）
     *
     * @param loginType 登录类型
     * @param userId 用户 ID
     */
    void onDisconnect(String loginType, String userId);
}
