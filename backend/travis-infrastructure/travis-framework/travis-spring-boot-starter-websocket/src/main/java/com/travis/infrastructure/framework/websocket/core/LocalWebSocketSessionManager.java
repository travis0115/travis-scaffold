package com.travis.infrastructure.framework.websocket.core;

import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.infrastructure.framework.websocket.config.WebSocketProperties;
import com.travis.infrastructure.framework.websocket.message.WebSocketMessage;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 本地 WebSocket Session 管理器，维护本实例上所有活跃连接。
 *
 * <p>使用 {@code loginType:userId} 复合键作为 Session 标识，支持多端（admin/app）同 ID 用户共存。
 *
 * <p>核心职责：
 *
 * <ul>
 *   <li>维护 sessionKey（loginType:userId）→ Set&lt;WebSocketSession&gt; 的本地映射
 *   <li>Session 注册/移除时同步更新 Redis 中的用户→实例映射
 *   <li>用户首次连接/最后断开时回调 {@link WebSocketSessionListener}
 *   <li>发送消息时：本地直接投递 + 通过 Redis Pub/Sub 广播到其他实例
 *   <li>定时心跳检测
 * </ul>
 *
 * @author travis
 */
@Slf4j
public class LocalWebSocketSessionManager extends TextWebSocketHandler
        implements WebSocketSessionManager {

    /** 本地 Session 存储：sessionKey（loginType:userId）→ sessions */
    private final ConcurrentMap<String, Set<WebSocketSession>> localSessions =
            new ConcurrentHashMap<>();

    private final WebSocketProperties properties;
    private final String instanceId;

    /** Redis 消息分发器，为 null 时降级为单实例模式 */
    @Nullable private final RedisWebSocketMessageDispatcher dispatcher;

    /** 业务层注册的会话生命周期监听器 */
    private final List<WebSocketSessionListener> listeners;

    private final ThreadPoolTaskScheduler heartbeatScheduler;
    private ScheduledFuture<?> heartbeatTask;

    public LocalWebSocketSessionManager(
            WebSocketProperties properties,
            @Nullable RedisWebSocketMessageDispatcher dispatcher,
            @Nullable List<WebSocketSessionListener> listeners) {
        this.properties = properties;
        this.instanceId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        this.dispatcher = dispatcher;
        this.listeners = listeners != null ? listeners : Collections.emptyList();
        this.heartbeatScheduler = createHeartbeatScheduler();
    }

    /** 启动心跳定时任务 */
    public void startHeartbeat() {
        if (properties.getHeartbeatInterval() > 0) {
            heartbeatTask =
                    heartbeatScheduler.scheduleAtFixedRate(
                            this::sendHeartbeat,
                            java.time.Duration.ofMillis(properties.getHeartbeatInterval()));
        }
    }

    /** 停止心跳定时任务（应用关闭时调用） */
    public void stopHeartbeat() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel(false);
        }
        heartbeatScheduler.destroy();
    }

    // ==================== WebSocketHandler ====================

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionKey = extractSessionKey(session);
        if (sessionKey == null) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        boolean isFirst =
                localSessions
                        .computeIfAbsent(sessionKey, k -> ConcurrentHashMap.newKeySet())
                        .add(session);
        if (dispatcher != null) {
            dispatcher.registerUserInstance(sessionKey, instanceId);
        }

        // 首次连接（从 0 → 1）时通知监听器
        if (isFirst) {
            fireConnect(sessionKey);
        }

        log.debug("[WebSocket] 连接建立: sessionKey={}, sessionId={}", sessionKey, session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message)
            throws Exception {
        String payload = message.getPayload();
        // 客户端回复 pong
        if ("pong".equalsIgnoreCase(payload) || "\"pong\"".equalsIgnoreCase(payload)) {
            return;
        }

        log.debug(
                "[WebSocket] 收到上行消息: sessionKey={}, payload={}",
                extractSessionKey(session),
                payload);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionKey = extractSessionKey(session);
        if (sessionKey == null) {
            return;
        }

        Set<WebSocketSession> sessions = localSessions.get(sessionKey);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                localSessions.remove(sessionKey);
                if (dispatcher != null) {
                    dispatcher.unregisterUserInstance(sessionKey, instanceId);
                }
                // 最后一个连接断开（从 1 → 0）时通知监听器
                fireDisconnect(sessionKey);
            }
        }
        log.debug(
                "[WebSocket] 连接关闭: sessionKey={}, sessionId={}, status={}",
                sessionKey,
                session.getId(),
                status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn(
                "[WebSocket] 传输错误: sessionKey={}, sessionId={}",
                extractSessionKey(session),
                session.getId(),
                exception);
    }

    // ==================== WebSocketSessionManager ====================

    @Override
    public void sendToUser(String loginType, String userId, WebSocketMessage message) {
        String sessionKey = loginType + ":" + userId;
        // 本地投递
        deliverToLocal(sessionKey, message);
        // 通过 Redis 广播到其他实例
        if (dispatcher != null) {
            dispatcher.publish(message);
        }
    }

    @Override
    public void sendToAll(WebSocketMessage message) {
        // 本地投递给所有连接
        localSessions.forEach((sessionKey, _) -> deliverToLocal(sessionKey, message));
        // 通过 Redis 广播到其他实例
        if (dispatcher != null) {
            dispatcher.publish(message);
        }
    }

    @Override
    public boolean isOnline(String loginType, String userId) {
        String sessionKey = loginType + ":" + userId;
        // 先查本地
        if (localSessions.containsKey(sessionKey)) {
            return true;
        }
        // 再查 Redis（集群范围）
        if (dispatcher != null) {
            return dispatcher.isUserOnline(sessionKey);
        }
        return false;
    }

    @Override
    public Set<String> getOnlineUsers() {
        if (dispatcher != null) {
            return dispatcher.getOnlineUsers();
        }
        return Collections.unmodifiableSet(localSessions.keySet());
    }

    // ==================== 内部方法 ====================

    /**
     * 将消息投递给本实例上的指定用户的所有 Session
     *
     * @param sessionKey 复合键（loginType:userId）
     * @param message 消息体
     */
    public void deliverToLocal(String sessionKey, WebSocketMessage message) {
        Set<WebSocketSession> sessions = localSessions.get(sessionKey);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        String json;
        try {
            json = JsonUtil.toJsonString(message);
        } catch (Exception e) {
            log.error("[WebSocket] 消息序列化失败: sessionKey={}", sessionKey, e);
            return;
        }

        TextMessage textMessage = new TextMessage(json);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    log.warn(
                            "[WebSocket] 消息发送失败: sessionKey={}, sessionId={}",
                            sessionKey,
                            session.getId(),
                            e);
                }
            }
        }
    }

    /**
     * 将广播消息投递给本实例上的所有 Session
     *
     * @param message 消息体
     */
    public void deliverToAllLocal(WebSocketMessage message) {
        localSessions.forEach((sessionKey, _) -> deliverToLocal(sessionKey, message));
    }

    /** 从 Session attributes 中提取 sessionKey（loginType:userId） */
    private String extractSessionKey(WebSocketSession session) {
        Object sessionKey = session.getAttributes().get("sessionKey");
        return sessionKey != null ? sessionKey.toString() : null;
    }

    /** 通知所有监听器：用户上线 */
    private void fireConnect(String sessionKey) {
        String[] parts = splitSessionKey(sessionKey);
        for (WebSocketSessionListener listener : listeners) {
            try {
                listener.onConnect(parts[0], parts[1]);
            } catch (Exception e) {
                log.warn("[WebSocket] Listener.onConnect 异常: sessionKey={}", sessionKey, e);
            }
        }
    }

    /** 通知所有监听器：用户下线 */
    private void fireDisconnect(String sessionKey) {
        String[] parts = splitSessionKey(sessionKey);
        for (WebSocketSessionListener listener : listeners) {
            try {
                listener.onDisconnect(parts[0], parts[1]);
            } catch (Exception e) {
                log.warn("[WebSocket] Listener.onDisconnect 异常: sessionKey={}", sessionKey, e);
            }
        }
    }

    /** 拆分 sessionKey：{@code loginType:userId} → [loginType, userId] */
    private String[] splitSessionKey(String sessionKey) {
        int idx = sessionKey.indexOf(':');
        if (idx < 0) {
            return new String[] {"unknown", sessionKey};
        }
        return new String[] {sessionKey.substring(0, idx), sessionKey.substring(idx + 1)};
    }

    /** 向所有本地连接发送心跳 */
    private void sendHeartbeat() {
        WebSocketMessage ping = WebSocketMessage.ping();
        String json;
        try {
            json = JsonUtil.toJsonString(ping);
        } catch (Exception e) {
            log.error("[WebSocket] 心跳消息序列化失败", e);
            return;
        }

        TextMessage textMessage = new TextMessage(json);
        localSessions.forEach(
                (sessionKey, sessions) -> {
                    for (WebSocketSession session : sessions) {
                        if (session.isOpen()) {
                            try {
                                session.sendMessage(textMessage);
                            } catch (IOException e) {
                                log.debug(
                                        "[WebSocket] 心跳发送失败: sessionKey={}, sessionId={}",
                                        sessionKey,
                                        session.getId());
                            }
                        }
                    }
                });
    }

    private ThreadPoolTaskScheduler createHeartbeatScheduler() {
        var scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("ws-heartbeat-");
        scheduler.setDaemon(true);
        scheduler.initialize();
        return scheduler;
    }
}
