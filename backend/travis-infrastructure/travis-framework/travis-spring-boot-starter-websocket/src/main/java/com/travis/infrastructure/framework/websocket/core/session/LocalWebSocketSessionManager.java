package com.travis.infrastructure.framework.websocket.core.session;

import com.travis.infrastructure.common.web.constant.HttpHeader;
import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.infrastructure.framework.websocket.config.properties.WebSocketProperties;
import com.travis.infrastructure.framework.websocket.core.auth.WebSocketAuthContext;
import com.travis.infrastructure.framework.websocket.core.auth.WebSocketAuthService;
import com.travis.infrastructure.framework.websocket.core.dispatch.RedisWebSocketMessageDispatcher;
import com.travis.infrastructure.framework.websocket.core.message.WebSocketMessage;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;

/**
 * 本地 WebSocket Session 管理器，维护本实例上所有活跃连接。
 *
 * <p>使用认证适配器返回的 principal 作为 Session 标识。
 *
 * <p>核心职责：
 *
 * <ul>
 *   <li>维护 principal → Set&lt;WebSocketSession&gt; 的本地映射
 *   <li>Session 注册/移除时同步更新 Redis 中的连接主体→实例映射
 *   <li>连接主体首次连接/最后断开时回调 {@link WebSocketSessionListener}
 *   <li>发送消息时：本地直接投递 + 通过 Redis Pub/Sub 广播到其他实例
 *   <li>定时心跳检测
 * </ul>
 *
 * @author travis
 */
@Slf4j
public class LocalWebSocketSessionManager extends TextWebSocketHandler
        implements WebSocketSessionManager {

    /** 本地 Session 存储：principal → sessions */
    private final ConcurrentMap<String, Set<WebSocketSession>> localSessions =
            new ConcurrentHashMap<>();

    private final WebSocketProperties properties;
    private final String instanceId;

    /** Redis 消息分发器，为 null 时降级为单实例模式 */
    @Nullable private final RedisWebSocketMessageDispatcher dispatcher;

    /** WebSocket 认证适配器 */
    @Nullable private final WebSocketAuthService authService;

    /** 业务层注册的会话生命周期监听器 */
    private final List<WebSocketSessionListener> listeners;

    private final ThreadPoolTaskScheduler heartbeatScheduler;
    private ScheduledFuture<?> heartbeatTask;
    private final ConcurrentMap<String, ScheduledFuture<?>> pendingDisconnectTasks =
            new ConcurrentHashMap<>();
    private static final String[] CLIENT_IP_HEADERS = {
        HttpHeader.X_FORWARDED_FOR,
        HttpHeader.X_REAL_IP,
        HttpHeader.PROXY_CLIENT_IP,
        HttpHeader.WL_PROXY_CLIENT_IP,
        HttpHeader.HTTP_CLIENT_IP,
        HttpHeader.HTTP_X_FORWARDED_FOR
    };

    public LocalWebSocketSessionManager(
            WebSocketProperties properties,
            @Nullable RedisWebSocketMessageDispatcher dispatcher,
            @Nullable WebSocketAuthService authService,
            @Nullable List<WebSocketSessionListener> listeners) {
        this.properties = properties;
        this.instanceId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        this.dispatcher = dispatcher;
        this.authService = authService;
        this.listeners = listeners != null ? listeners : Collections.emptyList();
        this.heartbeatScheduler = createHeartbeatScheduler();
    }

    /** 启动心跳定时任务 */
    public void startHeartbeat() {
        if (properties.getHeartbeatInterval() > 0) {
            heartbeatTask =
                    heartbeatScheduler.scheduleAtFixedRate(
                            this::sendHeartbeat,
                            Duration.ofMillis(properties.getHeartbeatInterval()));
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
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        String principal = extractPrincipal(session);
        if (principal == null) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        boolean wasConnected = isPrincipalConnected(principal);
        cancelPendingDisconnect(principal);
        boolean isFirst =
                localSessions
                        .computeIfAbsent(principal, k -> ConcurrentHashMap.newKeySet())
                        .add(session);
        if (dispatcher != null) {
            dispatcher.registerPrincipalInstance(principal, instanceId);
        }

        // 全局首次连接（从 0 → 1）时通知监听器
        if (isFirst && !wasConnected) {
            fireConnect(principal, getClientIp(session));
        }

        log.debug("[WebSocket] 连接建立: principal={}, sessionId={}", principal, session.getId());
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        // 客户端回复 pong
        if ("pong".equalsIgnoreCase(payload) || "\"pong\"".equalsIgnoreCase(payload)) {
            return;
        }

        log.debug(
                "[WebSocket] 收到上行消息: principal={}, payload={}", extractPrincipal(session), payload);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        String principal = extractPrincipal(session);
        if (principal == null) {
            return;
        }

        Set<WebSocketSession> sessions = localSessions.get(principal);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                localSessions.remove(principal);
                scheduleDisconnect(principal);
            }
        }
        log.debug(
                "[WebSocket] 连接关闭: principal={}, sessionId={}, status={}",
                principal,
                session.getId(),
                status);
    }

    @Override
    public void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) {
        log.warn(
                "[WebSocket] 传输错误: principal={}, sessionId={}",
                extractPrincipal(session),
                session.getId(),
                exception);
    }

    // ==================== WebSocketSessionManager ====================

    @Override
    public void sendToPrincipal(String principal, WebSocketMessage message) {
        // 本地投递
        deliverToLocal(principal, message);
        // 通过 Redis 广播到其他实例
        if (dispatcher != null) {
            dispatcher.publish(message);
        }
    }

    @Override
    public void sendToAll(WebSocketMessage message) {
        // 本地投递给所有连接
        localSessions.forEach((principal, _) -> deliverToLocal(principal, message));
        // 通过 Redis 广播到其他实例
        if (dispatcher != null) {
            dispatcher.publish(message);
        }
    }

    @Override
    public void close(String principal) {
        if (principal == null || principal.isBlank()) {
            return;
        }
        closeLocal(principal);
        if (dispatcher != null) {
            dispatcher.publish(WebSocketMessage.close(principal));
        }
    }

    @Override
    public boolean isConnected(String principal) {
        return isPrincipalConnected(principal);
    }

    private boolean isPrincipalConnected(String principal) {
        if (pendingDisconnectTasks.containsKey(principal)) {
            return true;
        }
        // 先查本地
        if (localSessions.containsKey(principal)) {
            return true;
        }
        // 再查 Redis（集群范围）
        if (dispatcher != null) {
            return dispatcher.isPrincipalConnected(principal);
        }
        return false;
    }

    @Override
    public Set<String> getConnectedPrincipals() {
        if (dispatcher != null) {
            return dispatcher.getConnectedPrincipals();
        }
        return Collections.unmodifiableSet(localSessions.keySet());
    }

    @Override
    public long countConnectedPrincipals() {
        if (dispatcher != null) {
            return dispatcher.countConnectedPrincipals();
        }
        return localSessions.size();
    }

    // ==================== 内部方法 ====================

    /**
     * 将消息投递给本实例上指定连接主体的所有 Session
     *
     * @param principal 连接主体
     * @param message 消息体
     */
    public void deliverToLocal(String principal, WebSocketMessage message) {
        Set<WebSocketSession> sessions = localSessions.get(principal);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        String json;
        try {
            json = JsonUtil.toJsonString(message);
        } catch (Exception e) {
            log.error("[WebSocket] 消息序列化失败: principal={}", principal, e);
            return;
        }

        TextMessage textMessage = new TextMessage(json);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    log.warn(
                            "[WebSocket] 消息发送失败: principal={}, sessionId={}",
                            principal,
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
        localSessions.forEach((principal, _) -> deliverToLocal(principal, message));
    }

    public void closeLocal(String principal) {
        Set<WebSocketSession> sessions = localSessions.get(principal);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        for (WebSocketSession session : new ArrayList<>(sessions)) {
            try {
                if (session.isOpen()) {
                    session.close(CloseStatus.NORMAL);
                }
            } catch (IOException e) {
                log.warn(
                        "[WebSocket] 关闭连接失败: principal={}, sessionId={}",
                        principal,
                        session.getId(),
                        e);
            }
        }
    }

    /** 从 Session attributes 中提取 principal */
    private String extractPrincipal(WebSocketSession session) {
        return WebSocketPrincipal.get(session.getAttributes());
    }

    /** 通知所有监听器：连接主体上线 */
    private void fireConnect(String principal, String ip) {
        for (WebSocketSessionListener listener : listeners) {
            try {
                listener.onConnect(principal, ip);
            } catch (Exception e) {
                log.warn("[WebSocket] Listener.onConnect 异常: principal={}", principal, e);
            }
        }
    }

    /** 通知所有监听器：连接主体下线 */
    private void fireDisconnect(String principal) {
        for (WebSocketSessionListener listener : listeners) {
            try {
                listener.onDisconnect(principal);
            } catch (Exception e) {
                log.warn("[WebSocket] Listener.onDisconnect 异常: principal={}", principal, e);
            }
        }
    }

    private void scheduleDisconnect(String principal) {
        cancelPendingDisconnect(principal);
        var task =
                heartbeatScheduler.schedule(
                        () -> confirmDisconnect(principal),
                        java.time.Instant.now()
                                .plusMillis(Math.max(properties.getOfflineGracePeriod(), 0)));
        pendingDisconnectTasks.put(principal, task);
    }

    private void cancelPendingDisconnect(String principal) {
        var task = pendingDisconnectTasks.remove(principal);
        if (task != null) {
            task.cancel(false);
        }
    }

    private void confirmDisconnect(String principal) {
        pendingDisconnectTasks.remove(principal);
        if (localSessions.containsKey(principal)) {
            return;
        }
        if (dispatcher != null) {
            dispatcher.unregisterPrincipalInstance(principal, instanceId);
            if (dispatcher.isPrincipalConnected(principal)) {
                return;
            }
        }
        fireDisconnect(principal);
    }

    private String getClientIp(WebSocketSession session) {
        var headers = session.getHandshakeHeaders();
        for (String header : CLIENT_IP_HEADERS) {
            var value = headers.getFirst(header);
            if (value != null && !value.isBlank() && !"unknown".equalsIgnoreCase(value)) {
                return value.split(",")[0].trim();
            }
        }
        var address = session.getRemoteAddress();
        return address == null || address.getAddress() == null
                ? null
                : address.getAddress().getHostAddress();
    }

    /** 向所有本地连接发送心跳 */
    private void sendHeartbeat() {
        closeInvalidTokenSessions();
        refreshLocalSessionTtl();

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
                (principal, sessions) -> {
                    for (WebSocketSession session : sessions) {
                        if (session.isOpen()) {
                            try {
                                session.sendMessage(textMessage);
                            } catch (IOException e) {
                                log.debug(
                                        "[WebSocket] 心跳发送失败: principal={}, sessionId={}",
                                        principal,
                                        session.getId());
                            }
                        }
                    }
                });
    }

    private void closeInvalidTokenSessions() {
        localSessions.forEach(
                (principal, sessions) -> {
                    for (WebSocketSession session : new ArrayList<>(sessions)) {
                        if (!session.isOpen() || isSessionTokenValid(session)) {
                            continue;
                        }
                        try {
                            session.close(CloseStatus.POLICY_VIOLATION);
                            log.debug(
                                    "[WebSocket] 连接认证已失效，关闭连接: principal={}, sessionId={}",
                                    principal,
                                    session.getId());
                        } catch (IOException e) {
                            log.warn(
                                    "[WebSocket] 关闭失效连接失败: principal={}, sessionId={}",
                                    principal,
                                    session.getId(),
                                    e);
                        }
                    }
                });
    }

    private boolean isSessionTokenValid(WebSocketSession session) {
        var attrs = session.getAttributes();
        var principal = WebSocketPrincipal.get(attrs);
        if (principal == null) {
            return false;
        }
        if (authService == null) {
            return false;
        }
        try {
            return authService.isConnectionValid(new WebSocketAuthContext(principal, attrs));
        } catch (Exception e) {
            return false;
        }
    }

    private void refreshLocalSessionTtl() {
        if (dispatcher == null) {
            return;
        }
        localSessions
                .keySet()
                .forEach(principal -> dispatcher.registerPrincipalInstance(principal, instanceId));
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
