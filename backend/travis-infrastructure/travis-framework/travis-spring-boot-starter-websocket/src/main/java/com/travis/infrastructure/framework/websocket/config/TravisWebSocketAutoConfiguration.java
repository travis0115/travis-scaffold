package com.travis.infrastructure.framework.websocket.config;

import com.travis.infrastructure.framework.redis.core.key.RedisKeyPrefixResolver;
import com.travis.infrastructure.framework.redis.core.pubsub.RedisPubSubClient;
import com.travis.infrastructure.framework.satoken.config.properties.SaTokenProperties;
import com.travis.infrastructure.framework.websocket.config.properties.WebSocketProperties;
import com.travis.infrastructure.framework.websocket.core.*;
import com.travis.infrastructure.framework.websocket.interceptor.WebSocketAuthInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.LinkedHashSet;

/**
 * WebSocket 自动配置类，注册 Bean 和 WebSocket 端点。
 *
 * <p>通过 {@code travis.websocket.enabled=true/false} 控制是否启用（默认启用）。
 *
 * <p>当引入 {@code travis-spring-boot-starter-redis} 且 Redis 可用时，自动启用集群广播模式； 否则降级为单实例模式。
 *
 * @author travis
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(WebSocketProperties.class)
@ConditionalOnProperty(
        prefix = "travis.websocket",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class TravisWebSocketAutoConfiguration {

    // ==================== Bean 注册 ====================

    /**
     * Redis 消息分发器。
     *
     * <p>通过 setter 注入 {@link LocalWebSocketSessionManager} 来打破循环依赖。
     */
    @Bean
    public RedisWebSocketMessageDispatcher redisWebSocketMessageDispatcher(
            RedisPubSubClient redisPubSubClient,
            RedisTemplate<String, Object> redisTemplate,
            RedisKeyPrefixResolver redisKeyPrefixResolver,
            WebSocketProperties properties) {
        var dispatcher =
                new RedisWebSocketMessageDispatcher(
                        redisPubSubClient, redisTemplate, redisKeyPrefixResolver, properties);
        dispatcher.subscribe();
        return dispatcher;
    }

    @Bean
    @ConditionalOnMissingBean
    public WebSocketTicketService webSocketTicketService(
            RedisTemplate<String, Object> redisTemplate,
            RedisKeyPrefixResolver redisKeyPrefixResolver,
            WebSocketProperties properties) {
        return new WebSocketTicketService(redisTemplate, redisKeyPrefixResolver, properties);
    }

    /**
     * 本地 Session 管理器。
     *
     * <p>通过 {@link ObjectProvider} 可选注入 {@link RedisWebSocketMessageDispatcher}： Redis
     * 可用时启用集群广播，不可用时降级为单实例模式。
     */
    @Bean
    @ConditionalOnMissingBean
    public LocalWebSocketSessionManager localSessionManager(
            WebSocketProperties properties,
            ObjectProvider<RedisWebSocketMessageDispatcher> dispatcherProvider,
            ObjectProvider<WebSocketSessionListener> listenerProvider) {
        var dispatcher = dispatcherProvider.getIfAvailable();
        var listeners = listenerProvider.orderedStream().toList();
        if (dispatcher != null) {
            log.info("[WebSocket] 集群模式已启用（Redis Pub/Sub）");
        } else {
            log.info("[WebSocket] 单实例模式（Redis 未引入）");
        }
        if (!listeners.isEmpty()) {
            log.info("[WebSocket] 已注册 {} 个 SessionListener", listeners.size());
        }

        var manager = new LocalWebSocketSessionManager(properties, dispatcher, listeners);
        if (dispatcher != null) {
            dispatcher.setSessionManager(manager);
        }
        manager.startHeartbeat();
        return manager;
    }

    /** WebSocketSessionManager 接口暴露 */
    @Bean
    @ConditionalOnMissingBean
    public WebSocketSessionManager webSocketSessionManager(LocalWebSocketSessionManager manager) {
        return manager;
    }

    /** 消息发送工具 */
    @Bean
    @ConditionalOnMissingBean
    public WebSocketMessageSender webSocketMessageSender(WebSocketSessionManager sessionManager) {
        return new WebSocketMessageSender(sessionManager);
    }

    // ==================== WebSocket 端点注册 ====================

    /**
     * 内部配置类，负责注册 WebSocket 端点。
     *
     * <p>拆为独立 {@link Configuration} 类，通过构造器注入已创建好的 Bean，避免与外层 {@code @Bean} 工厂方法产生循环依赖。
     */
    @Configuration
    @EnableWebSocket
    static class WebSocketEndpointConfigurer implements WebSocketConfigurer {

        private final WebSocketProperties properties;
        private final LocalWebSocketSessionManager sessionManager;
        private final WebSocketTicketService ticketService;
        private final ObjectProvider<SaTokenProperties> saTokenPropertiesProvider;

        public WebSocketEndpointConfigurer(
                WebSocketProperties properties,
                LocalWebSocketSessionManager sessionManager,
                WebSocketTicketService ticketService,
                ObjectProvider<SaTokenProperties> saTokenPropertiesProvider) {
            this.properties = properties;
            this.sessionManager = sessionManager;
            this.ticketService = ticketService;
            this.saTokenPropertiesProvider = saTokenPropertiesProvider;
        }

        @Override
        public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
            var loginTypes = getLoginTypes();
            for (String loginType : loginTypes) {
                var path = buildEndpointPath(loginType);
                registry.addHandler(sessionManager, path)
                        .setAllowedOrigins(properties.getAllowedOrigins())
                        .addInterceptors(new WebSocketAuthInterceptor(loginType, ticketService));
                log.info(
                        "[WebSocket] 端点已注册: path={}, loginType={}, allowedOrigins={}, auth=Sa-Token",
                        path,
                        loginType,
                        properties.getAllowedOrigins());
            }
        }

        private LinkedHashSet<String> getLoginTypes() {
            var loginTypes = new LinkedHashSet<String>();
            var saTokenProperties = saTokenPropertiesProvider.getIfAvailable();
            if (saTokenProperties != null) {
                for (var rule : saTokenProperties.getAuthRules()) {
                    if (rule.getLoginType() != null && !rule.getLoginType().isBlank()) {
                        loginTypes.add(rule.getLoginType().trim());
                    }
                }
            }
            return loginTypes;
        }

        private String buildEndpointPath(String loginType) {
            var path = properties.getPath();
            if (path.endsWith("/")) {
                return path + loginType;
            }
            return path + "/" + loginType;
        }
    }
}
