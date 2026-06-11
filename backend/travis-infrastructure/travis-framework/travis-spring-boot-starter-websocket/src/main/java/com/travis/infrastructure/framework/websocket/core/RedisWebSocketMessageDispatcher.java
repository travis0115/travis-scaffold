package com.travis.infrastructure.framework.websocket.core;

import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.infrastructure.framework.websocket.config.WebSocketProperties;
import com.travis.infrastructure.framework.websocket.message.WebSocketMessage;
import com.travis.infrastructure.framework.websocket.message.WebSocketMessageType;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * 基于 Redis Pub/Sub 的 WebSocket 消息分发器，实现多实例集群广播。
 *
 * <p>工作流程：
 *
 * <ol>
 *   <li>业务调用 {@code sendToUser(userId, msg)} → 本地投递 + Redis Pub/Sub 发布
 *   <li>所有实例（包括自己）收到 Redis 消息 → 检查本地是否有目标用户的连接 → 有则投递
 *   <li>用户上线/下线 → Redis Set 维护 userId → instanceIds 映射
 * </ol>
 *
 * <p>当 Redis 不可用时自动降级为单实例模式（仅本地投递）。
 *
 * @author travis
 */
@Slf4j
public class RedisWebSocketMessageDispatcher implements MessageListener {

    private final RedisTemplate<String, Object> redisTemplate;
    private final WebSocketProperties properties;
    private final String instanceId;

    /** 通过 setter 注入，避免与 LocalWebSocketSessionManager 的循环依赖 */
    private LocalWebSocketSessionManager sessionManager;

    /** 标记 Redis 是否可用，不可用时降级为单实例模式 */
    private volatile boolean redisAvailable = true;

    public RedisWebSocketMessageDispatcher(
            RedisTemplate<String, Object> redisTemplate, WebSocketProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.instanceId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    /** 注入 LocalWebSocketSessionManager（由 AutoConfiguration 调用） */
    public void setSessionManager(LocalWebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /** 订阅 Redis Pub/Sub 频道 */
    public void subscribe(RedisMessageListenerContainer container) {
        container.addMessageListener(this, new ChannelTopic(properties.getRedis().getChannel()));
        log.info(
                "[WebSocket] 已订阅 Redis 频道: channel={}, instanceId={}",
                properties.getRedis().getChannel(),
                instanceId);
    }

    // ==================== 消息发布 ====================

    /**
     * 通过 Redis Pub/Sub 发布消息到所有实例
     *
     * @param message WebSocket 消息
     */
    public void publish(WebSocketMessage message) {
        if (!redisAvailable) {
            return;
        }
        try {
            String json = JsonUtil.toJsonString(message);
            redisTemplate.convertAndSend(properties.getRedis().getChannel(), json);
        } catch (Exception e) {
            redisAvailable = false;
            log.warn("[WebSocket] Redis Pub/Sub 发布失败，降级为单实例模式", e);
        }
    }

    // ==================== Redis Pub/Sub 消费 ====================

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String json = new String(message.getBody(), StandardCharsets.UTF_8);
            WebSocketMessage wsMessage = JsonUtil.parseObject(json, WebSocketMessage.class);

            if (wsMessage == null || sessionManager == null) {
                return;
            }

            // 跳过心跳消息的广播处理
            if (wsMessage.getType() == WebSocketMessageType.PING
                    || wsMessage.getType() == WebSocketMessageType.PONG) {
                return;
            }

            // 根据消息类型投递到本地 Session
            if (wsMessage.getType() == WebSocketMessageType.BROADCAST) {
                sessionManager.deliverToAllLocal(wsMessage);
            } else if (wsMessage.getType() == WebSocketMessageType.USER
                    && wsMessage.getLoginType() != null
                    && wsMessage.getToUser() != null) {
                String sessionKey = wsMessage.getLoginType() + ":" + wsMessage.getToUser();
                sessionManager.deliverToLocal(sessionKey, wsMessage);
            }
        } catch (Exception e) {
            log.error("[WebSocket] Redis 消息处理失败", e);
        }
    }

    // ==================== 用户在线状态管理 ====================

    /**
     * 注册用户→实例映射（用户上线时调用）
     *
     * @param userId 用户 ID
     * @param instanceId 当前实例 ID
     */
    public void registerUserInstance(String userId, String instanceId) {
        if (!redisAvailable) {
            return;
        }
        try {
            String key = buildSessionKey(userId);
            redisTemplate.opsForSet().add(key, instanceId);
            // 设置过期时间，防止僵尸 key（超时时间是心跳间隔的 3 倍）
            long ttl =
                    Math.max(properties.getSessionTimeout(), properties.getHeartbeatInterval() * 3);
            redisTemplate.expire(key, java.time.Duration.ofMillis(ttl));
            // 每次注册时刷新可用状态
            redisAvailable = true;
        } catch (Exception e) {
            redisAvailable = false;
            log.warn("[WebSocket] Redis 注册用户实例映射失败", e);
        }
    }

    /**
     * 移除用户→实例映射（用户下线时调用）
     *
     * @param userId 用户 ID
     * @param instanceId 当前实例 ID
     */
    public void unregisterUserInstance(String userId, String instanceId) {
        if (!redisAvailable) {
            return;
        }
        try {
            String key = buildSessionKey(userId);
            redisTemplate.opsForSet().remove(key, instanceId);
            // 如果集合为空则删除 key
            Long size = redisTemplate.opsForSet().size(key);
            if (size != null && size == 0) {
                redisTemplate.delete(key);
            }
        } catch (Exception e) {
            log.warn("[WebSocket] Redis 移除用户实例映射失败", e);
        }
    }

    /**
     * 判断用户是否在线（集群范围，查 Redis）
     *
     * @param userId 用户 ID
     * @return 是否在线
     */
    public boolean isUserOnline(String userId) {
        if (!redisAvailable) {
            return false;
        }
        try {
            String key = buildSessionKey(userId);
            Long size = redisTemplate.opsForSet().size(key);
            return size != null && size > 0;
        } catch (Exception e) {
            log.warn("[WebSocket] Redis 查询用户在线状态失败", e);
            return false;
        }
    }

    /**
     * 获取所有在线用户 ID（集群范围）
     *
     * @return 在线用户 ID 集合
     */
    public Set<String> getOnlineUsers() {
        if (!redisAvailable) {
            return Collections.emptySet();
        }
        try {
            return redisTemplate.keys(buildSessionKey("*")).stream()
                    .map(
                            key -> {
                                String prefix = properties.getRedis().getSessionKeyPrefix();
                                return key.substring(prefix.length());
                            })
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.warn("[WebSocket] Redis 获取在线用户列表失败", e);
            return Collections.emptySet();
        }
    }

    /** 获取当前实例 ID */
    public String getInstanceId() {
        return instanceId;
    }

    private String buildSessionKey(String userId) {
        return properties.getRedis().getSessionKeyPrefix() + userId;
    }
}
