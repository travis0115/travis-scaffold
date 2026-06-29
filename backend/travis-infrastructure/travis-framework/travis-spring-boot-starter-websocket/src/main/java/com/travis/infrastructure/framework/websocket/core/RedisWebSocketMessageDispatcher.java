package com.travis.infrastructure.framework.websocket.core;

import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.infrastructure.framework.redis.core.key.RedisKeyPrefixResolver;
import com.travis.infrastructure.framework.redis.core.pubsub.RedisPubSubClient;
import com.travis.infrastructure.framework.websocket.config.properties.WebSocketProperties;
import com.travis.infrastructure.framework.websocket.message.WebSocketMessage;
import com.travis.infrastructure.framework.websocket.message.WebSocketMessageType;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 基于 Redis Pub/Sub 的 WebSocket 消息分发器，实现多实例集群广播。
 *
 * <p>工作流程：
 *
 * <ol>
 *   <li>业务调用 {@code sendToPrincipal(principal, msg)} → 本地投递 + Redis Pub/Sub 发布
 *   <li>所有实例（包括自己）收到 Redis 消息 → 检查本地是否有目标连接主体的连接 → 有则投递
 *   <li>连接主体上线/下线 → Redis Set 维护 principal → instanceIds 映射
 * </ol>
 *
 * <p>当 Redis Pub/Sub 不可用或未启用时降级为单实例模式（仅本地投递）。
 *
 * @author travis
 */
@Slf4j
public class RedisWebSocketMessageDispatcher {

    private final RedisPubSubClient redisPubSubClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisKeyPrefixResolver redisKeyPrefixResolver;
    private final WebSocketProperties properties;
    private final String instanceId;

    /** 通过 setter 注入，避免与 LocalWebSocketSessionManager 的循环依赖 */
    private LocalWebSocketSessionManager sessionManager;

    /** 标记 Redis 是否可用，不可用时降级为单实例模式 */
    private volatile boolean redisAvailable = true;

    public RedisWebSocketMessageDispatcher(
            RedisPubSubClient redisPubSubClient,
            RedisTemplate<String, Object> redisTemplate,
            RedisKeyPrefixResolver redisKeyPrefixResolver,
            WebSocketProperties properties) {
        this.redisPubSubClient = redisPubSubClient;
        this.redisTemplate = redisTemplate;
        this.redisKeyPrefixResolver = redisKeyPrefixResolver;
        this.properties = properties;
        this.instanceId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    /** 注入 LocalWebSocketSessionManager（由 AutoConfiguration 调用） */
    public void setSessionManager(LocalWebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /** 订阅 WebSocket 集群广播频道 */
    public void subscribe() {
        redisPubSubClient.subscribe(
                properties.getRedis().getChannel(), (_, payload) -> onMessage(payload));
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
            redisPubSubClient.publish(properties.getRedis().getChannel(), json);
        } catch (Exception e) {
            redisAvailable = false;
            log.warn("[WebSocket] Redis Pub/Sub 发布失败，降级为单实例模式", e);
        }
    }

    // ==================== WebSocket 集群消息消费 ====================

    private void onMessage(String json) {
        try {
            WebSocketMessage wsMessage = JsonUtil.parseObject(json, WebSocketMessage.class);

            if (wsMessage == null || sessionManager == null) {
                return;
            }

            // 跳过心跳消息的广播处理
            if (wsMessage.getType() == WebSocketMessageType.PING
                    || wsMessage.getType() == WebSocketMessageType.PONG) {
                return;
            }

            if (wsMessage.getType() == WebSocketMessageType.CLOSE && wsMessage.getTo() != null) {
                sessionManager.closeLocal(wsMessage.getTo());
                return;
            }

            // 根据消息类型投递到本地 Session
            if (wsMessage.getType() == WebSocketMessageType.BROADCAST) {
                sessionManager.deliverToAllLocal(wsMessage);
            } else if (wsMessage.getType() == WebSocketMessageType.USER
                    && wsMessage.getTo() != null) {
                sessionManager.deliverToLocal(wsMessage.getTo(), wsMessage);
            }
        } catch (Exception e) {
            log.error("[WebSocket] Redis 消息处理失败", e);
        }
    }

    // ==================== 用户在线状态管理 ====================

    /**
     * 注册连接主体→实例映射（连接主体上线时调用）
     *
     * @param principal 连接主体
     * @param instanceId 当前实例 ID
     */
    public void registerPrincipalInstance(String principal, String instanceId) {
        if (!redisAvailable) {
            return;
        }
        try {
            String key = buildSessionKey(principal);
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
     * 移除连接主体→实例映射（连接主体下线时调用）
     *
     * @param principal 连接主体
     * @param instanceId 当前实例 ID
     */
    public void unregisterPrincipalInstance(String principal, String instanceId) {
        if (!redisAvailable) {
            return;
        }
        try {
            String key = buildSessionKey(principal);
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
     * 判断连接主体是否在线（集群范围，查 Redis）
     *
     * @param principal 连接主体
     * @return 是否在线
     */
    public boolean isPrincipalOnline(String principal) {
        if (!redisAvailable) {
            return false;
        }
        try {
            String key = buildSessionKey(principal);
            Long size = redisTemplate.opsForSet().size(key);
            return size != null && size > 0;
        } catch (Exception e) {
            log.warn("[WebSocket] Redis 查询用户在线状态失败", e);
            return false;
        }
    }

    /**
     * 获取所有在线连接主体（集群范围）
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
                                String businessKey = redisKeyPrefixResolver.remove(key);
                                String sessionPrefix = sessionKeyPrefix();
                                return businessKey.substring(sessionPrefix.length());
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

    private String buildSessionKey(String principal) {
        return redisKeyPrefixResolver.apply(sessionKeyPrefix() + principal);
    }

    private String sessionKeyPrefix() {
        String prefix = properties.getRedis().getSessionKeyPrefix();
        return prefix.endsWith(":") ? prefix : prefix + ":";
    }
}
