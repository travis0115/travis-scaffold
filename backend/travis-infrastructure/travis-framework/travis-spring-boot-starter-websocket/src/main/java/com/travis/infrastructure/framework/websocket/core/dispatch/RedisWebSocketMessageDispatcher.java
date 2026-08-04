package com.travis.infrastructure.framework.websocket.core.dispatch;

import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.infrastructure.framework.redis.core.key.RedisKeyPrefixResolver;
import com.travis.infrastructure.framework.redis.core.pubsub.RedisPubSubClient;
import com.travis.infrastructure.framework.websocket.config.properties.WebSocketProperties;
import com.travis.infrastructure.framework.websocket.core.message.WebSocketMessage;
import com.travis.infrastructure.framework.websocket.core.message.WebSocketMessageType;
import com.travis.infrastructure.framework.websocket.core.session.LocalWebSocketSessionManager;
import com.travis.infrastructure.framework.websocket.core.session.WebSocketNamespace;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
 *   <li>连接主体上线/下线 → Redis ZSet 维护 principal → instanceId 过期时间映射
 * </ol>
 *
 * <p>当 Redis Pub/Sub 不可用或未启用时降级为单实例模式（仅本地投递）。
 *
 * @author travis
 */
@Slf4j
@RequiredArgsConstructor
public class RedisWebSocketMessageDispatcher {

    private final RedisPubSubClient redisPubSubClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisKeyPrefixResolver redisKeyPrefixResolver;
    private final WebSocketProperties properties;

    /** -- GETTER -- 获取当前实例 ID */
    @Getter
    private final String instanceId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

    /**
     * 通过 setter 注入，避免与 LocalWebSocketSessionManager 的循环依赖 -- SETTER -- 注入
     * LocalWebSocketSessionManager（由 AutoConfiguration 调用）
     */
    @Setter private LocalWebSocketSessionManager sessionManager;

    /** 标记 Redis 是否可用，不可用时降级为单实例模式 */
    private volatile boolean redisAvailable = true;

    /** Redis 下次允许重试时间 */
    private volatile long nextRetryAt = 0;

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
        if (!shouldTryRedis()) {
            return;
        }
        String previousSourceInstanceId = message.getSourceInstanceId();
        try {
            message.setSourceInstanceId(instanceId);
            String json = JsonUtil.toJsonString(message);
            redisPubSubClient.publish(properties.getRedis().getChannel(), json);
            markRedisAvailable();
        } catch (Exception e) {
            markRedisUnavailable("[WebSocket] Redis Pub/Sub 发布失败，短暂降级为单实例模式", e);
        } finally {
            message.setSourceInstanceId(previousSourceInstanceId);
        }
    }

    // ==================== WebSocket 集群消息消费 ====================

    private void onMessage(String json) {
        try {
            WebSocketMessage wsMessage = JsonUtil.parseObject(json, WebSocketMessage.class);

            if (wsMessage == null || sessionManager == null) {
                return;
            }
            if (instanceId.equals(wsMessage.getSourceInstanceId())) {
                return;
            }
            wsMessage.setSourceInstanceId(null);

            // 跳过心跳消息的广播处理
            if (wsMessage.getType() == WebSocketMessageType.PING
                    || wsMessage.getType() == WebSocketMessageType.PONG) {
                return;
            }

            if (wsMessage.getType() == WebSocketMessageType.CLOSE && wsMessage.getTo() != null) {
                var attributeMatcher = parseAttributeMatcher(wsMessage.getContent());
                if (attributeMatcher == null) {
                    sessionManager.closeLocal(wsMessage.getTo());
                } else {
                    sessionManager.closeLocal(
                            wsMessage.getTo(),
                            attributeMatcher.attributeName(),
                            attributeMatcher.attributeValue());
                }
                return;
            }
            if (wsMessage.getType() == WebSocketMessageType.CLOSE_IMMEDIATELY
                    && wsMessage.getTo() != null) {
                sessionManager.closeLocalImmediately(wsMessage.getTo());
                return;
            }

            // 根据消息类型投递到本地 Session
            if (wsMessage.getType() == WebSocketMessageType.BROADCAST) {
                sessionManager.deliverToAllLocal(wsMessage);
            } else if (wsMessage.getType() == WebSocketMessageType.NAMESPACE
                    && wsMessage.getTo() != null) {
                sessionManager.deliverToNamespaceLocal(wsMessage.getTo(), wsMessage);
            } else if (wsMessage.getType() == WebSocketMessageType.USER
                    && wsMessage.getTo() != null) {
                sessionManager.deliverToLocal(wsMessage.getTo(), wsMessage);
            }
        } catch (Exception e) {
            log.error("[WebSocket] Redis 消息处理失败", e);
        }
    }

    private AttributeMatcher parseAttributeMatcher(Object content) {
        if (!(content instanceof Map<?, ?> map)) {
            return null;
        }
        Object name = map.get("attributeName");
        Object value = map.get("attributeValue");
        if (name == null || value == null || name.toString().isBlank()) {
            return null;
        }
        return new AttributeMatcher(name.toString(), value);
    }

    private record AttributeMatcher(String attributeName, Object attributeValue) {}

    // ==================== 连接主体状态管理 ====================

    /**
     * 注册连接主体→实例映射（连接主体连接时调用）
     *
     * @param principal 连接主体
     * @param instanceId 当前实例 ID
     */
    public void registerPrincipalInstance(String namespace, String principal, String instanceId) {
        if (!shouldTryRedis()) {
            return;
        }
        try {
            String key = buildSessionKey(principal);
            long ttl = sessionTtl();
            redisTemplate.opsForZSet().add(key, instanceId, expireAtMillis(ttl));
            redisTemplate.expire(key, Duration.ofMillis(ttl));
            redisTemplate
                    .opsForZSet()
                    .add(
                            buildConnectedPrincipalIndexKey(namespace),
                            principal,
                            expireAtMillis(ttl));
            markRedisAvailable();
        } catch (Exception e) {
            markRedisUnavailable("[WebSocket] Redis 注册连接主体实例映射失败，短暂降级为单实例模式", e);
        }
    }

    /**
     * 移除连接主体→实例映射（连接主体断开时调用）
     *
     * @param principal 连接主体
     * @param instanceId 当前实例 ID
     */
    public void unregisterPrincipalInstance(String namespace, String principal, String instanceId) {
        if (!shouldTryRedis()) {
            return;
        }
        try {
            String key = buildSessionKey(principal);
            redisTemplate.opsForZSet().remove(key, instanceId);
            removeExpiredPrincipalInstances(key);
            Long size = redisTemplate.opsForZSet().zCard(key);
            if (size != null && size == 0) {
                redisTemplate.delete(key);
                redisTemplate
                        .opsForZSet()
                        .remove(buildConnectedPrincipalIndexKey(namespace), principal);
            }
            markRedisAvailable();
        } catch (Exception e) {
            markRedisUnavailable("[WebSocket] Redis 移除连接主体实例映射失败，短暂降级为单实例模式", e);
        }
    }

    /**
     * 判断连接主体是否已连接（集群范围，查 Redis）
     *
     * @param principal 连接主体
     * @return 是否已连接
     */
    public boolean isPrincipalConnected(String principal) {
        if (!shouldTryRedis()) {
            return false;
        }
        try {
            String key = buildSessionKey(principal);
            removeExpiredPrincipalInstances(key);
            Long size = redisTemplate.opsForZSet().zCard(key);
            markRedisAvailable();
            return size != null && size > 0;
        } catch (Exception e) {
            markRedisUnavailable("[WebSocket] Redis 查询连接主体状态失败，短暂降级为单实例模式", e);
            return false;
        }
    }

    /**
     * 获取指定命名空间下所有已连接主体（集群范围）
     *
     * @param namespace 连接命名空间
     * @return 已连接主体集合
     */
    public Set<String> getConnectedPrincipals(String namespace) {
        if (!shouldTryRedis()) {
            return Collections.emptySet();
        }
        try {
            removeExpiredConnectedPrincipals(namespace);
            var principals =
                    redisTemplate
                            .opsForZSet()
                            .rangeByScore(
                                    buildConnectedPrincipalIndexKey(namespace),
                                    System.currentTimeMillis(),
                                    Double.POSITIVE_INFINITY);
            markRedisAvailable();
            if (principals == null || principals.isEmpty()) {
                return Collections.emptySet();
            }
            return principals.stream().map(Object::toString).collect(Collectors.toSet());
        } catch (Exception e) {
            markRedisUnavailable("[WebSocket] Redis 获取连接主体列表失败，短暂降级为单实例模式", e);
            return Collections.emptySet();
        }
    }

    /**
     * 获取指定命名空间下已连接主体数量（集群范围）
     *
     * @param namespace 连接命名空间
     * @return 已连接主体数量
     */
    public long countConnectedPrincipals(String namespace) {
        if (!shouldTryRedis()) {
            return 0L;
        }
        try {
            removeExpiredConnectedPrincipals(namespace);
            var count =
                    redisTemplate
                            .opsForZSet()
                            .count(
                                    buildConnectedPrincipalIndexKey(namespace),
                                    System.currentTimeMillis(),
                                    Double.POSITIVE_INFINITY);
            markRedisAvailable();
            return count == null ? 0L : count;
        } catch (Exception e) {
            markRedisUnavailable("[WebSocket] Redis 统计连接主体数量失败，短暂降级为单实例模式", e);
            return 0L;
        }
    }

    private boolean shouldTryRedis() {
        return redisAvailable || System.currentTimeMillis() >= nextRetryAt;
    }

    private void markRedisAvailable() {
        redisAvailable = true;
        nextRetryAt = 0;
    }

    private void markRedisUnavailable(String message, Exception e) {
        redisAvailable = false;
        nextRetryAt =
                System.currentTimeMillis() + Math.max(properties.getRedis().getRetryInterval(), 0);
        log.warn(message, e);
    }

    private String buildSessionKey(String principal) {
        return redisKeyPrefixResolver.apply(sessionKeyPrefix() + principal);
    }

    private String buildConnectedPrincipalIndexKey(String namespace) {
        return redisKeyPrefixResolver.apply(
                sessionKeyPrefix() + normalizeNamespace(namespace) + ":_index");
    }

    private String sessionKeyPrefix() {
        String prefix = properties.getRedis().getSessionKeyPrefix();
        return prefix.endsWith(":") ? prefix : prefix + ":";
    }

    private long sessionTtl() {
        return Math.max(properties.getSessionTimeout(), properties.getHeartbeatInterval() * 3);
    }

    private double expireAtMillis(long ttl) {
        return System.currentTimeMillis() + ttl;
    }

    private void removeExpiredPrincipalInstances(String key) {
        redisTemplate
                .opsForZSet()
                .removeRangeByScore(key, Double.NEGATIVE_INFINITY, System.currentTimeMillis());
    }

    private String normalizeNamespace(String namespace) {
        return namespace == null || namespace.isBlank()
                ? WebSocketNamespace.DEFAULT_NAMESPACE
                : namespace;
    }

    private void removeExpiredConnectedPrincipals(String namespace) {
        redisTemplate
                .opsForZSet()
                .removeRangeByScore(
                        buildConnectedPrincipalIndexKey(namespace),
                        Double.NEGATIVE_INFINITY,
                        System.currentTimeMillis());
    }
}
