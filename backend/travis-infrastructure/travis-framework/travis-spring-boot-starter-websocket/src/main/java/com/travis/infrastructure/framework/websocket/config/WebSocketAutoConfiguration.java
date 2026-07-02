package com.travis.infrastructure.framework.websocket.config;

import com.travis.infrastructure.framework.redis.core.key.RedisKeyPrefixResolver;
import com.travis.infrastructure.framework.redis.core.pubsub.RedisPubSubClient;
import com.travis.infrastructure.framework.web.config.properties.WebProperties;
import com.travis.infrastructure.framework.websocket.config.properties.WebSocketProperties;
import com.travis.infrastructure.framework.websocket.core.auth.WebSocketAuthService;
import com.travis.infrastructure.framework.websocket.core.dispatch.RedisWebSocketMessageDispatcher;
import com.travis.infrastructure.framework.websocket.core.endpoint.WebSocketEndpoint;
import com.travis.infrastructure.framework.websocket.core.endpoint.WebSocketEndpointProvider;
import com.travis.infrastructure.framework.websocket.core.sender.WebSocketMessageSender;
import com.travis.infrastructure.framework.websocket.core.session.LocalWebSocketSessionManager;
import com.travis.infrastructure.framework.websocket.core.session.WebSocketSessionListener;
import com.travis.infrastructure.framework.websocket.core.session.WebSocketSessionManager;
import com.travis.infrastructure.framework.websocket.core.interceptor.WebSocketAuthInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.stream.Stream;

/**
 * WebSocket 自动配置类，注册 Bean 和 WebSocket 端点。
 *
 * <p>通过 {@code travis.web.websocket.enabled=true/false} 控制是否启用（默认启用）。
 *
 * <p>当 {@code travis.web.websocket.redis.enabled=true} 且 Redis 可用时启用集群广播模式；否则降级为单实例模式。
 *
 * @author travis
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties({WebSocketProperties.class, WebProperties.class})
@ConditionalOnProperty(
        prefix = "travis.web.websocket",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class WebSocketAutoConfiguration {

    // ==================== Bean 注册 ====================

    /**
     * 本地 Session 管理器。
     *
     * <p>通过 {@link ObjectProvider} 可选注入 {@link RedisWebSocketMessageDispatcher}： Redis
     * 可用时启用集群广播，不可用时降级为单实例模式。
     */
    @Bean(destroyMethod = "stopHeartbeat")
    @ConditionalOnMissingBean
    public LocalWebSocketSessionManager localSessionManager(
            WebSocketProperties properties,
            ObjectProvider<RedisWebSocketMessageDispatcher> dispatcherProvider,
            ObjectProvider<WebSocketAuthService> authServiceProvider,
            ObjectProvider<WebSocketSessionListener> listenerProvider) {
        var dispatcher = dispatcherProvider.getIfAvailable();
        var authService = authServiceProvider.getIfAvailable();
        var listeners = listenerProvider.orderedStream().toList();
        if (dispatcher != null) {
            log.info("[WebSocket] 集群模式已启用（Redis Pub/Sub）");
        } else {
            log.info("[WebSocket] 单实例模式（Redis Pub/Sub 未启用）");
        }
        if (!listeners.isEmpty()) {
            log.info("[WebSocket] 已注册 {} 个 SessionListener", listeners.size());
        }

        var manager =
                new LocalWebSocketSessionManager(properties, dispatcher, authService, listeners);
        if (dispatcher != null) {
            dispatcher.setSessionManager(manager);
        }
        manager.startHeartbeat();
        return manager;
    }

    /** Redis 相关能力：只有项目引入并成功装配 Redis starter 后才启用。 */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(
            name = {
                "org.springframework.data.redis.core.RedisTemplate",
                "com.travis.infrastructure.framework.redis.core.key.RedisKeyPrefixResolver",
                "com.travis.infrastructure.framework.redis.core.pubsub.RedisPubSubClient"
            })
    static class RedisWebSocketConfiguration {

        /**
         * Redis 消息分发器。
         *
         * <p>通过 setter 注入 {@link LocalWebSocketSessionManager} 来打破循环依赖。
         */
        @Bean
        @ConditionalOnBean({
            RedisPubSubClient.class,
            RedisTemplate.class,
            RedisKeyPrefixResolver.class
        })
        @ConditionalOnProperty(
                prefix = "travis.web.websocket.redis",
                name = "enabled",
                havingValue = "true",
                matchIfMissing = true)
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
        private final WebProperties webProperties;
        private final LocalWebSocketSessionManager sessionManager;
        private final ObjectProvider<WebSocketAuthService> authServiceProvider;
        private final ObjectProvider<WebSocketEndpoint> endpointProvider;
        private final ObjectProvider<WebSocketEndpointProvider> endpointProviderProvider;

        public WebSocketEndpointConfigurer(
                WebSocketProperties properties,
                WebProperties webProperties,
                LocalWebSocketSessionManager sessionManager,
                ObjectProvider<WebSocketAuthService> authServiceProvider,
                ObjectProvider<WebSocketEndpoint> endpointProvider,
                ObjectProvider<WebSocketEndpointProvider> endpointProviderProvider) {
            this.properties = properties;
            this.webProperties = webProperties;
            this.sessionManager = sessionManager;
            this.authServiceProvider = authServiceProvider;
            this.endpointProvider = endpointProvider;
            this.endpointProviderProvider = endpointProviderProvider;
        }

        @Override
        public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
            var authService = authServiceProvider.getIfAvailable();
            if (authService == null) {
                log.warn("[WebSocket] 未注册 WebSocketAuthService，跳过端点注册");
                return;
            }
            var endpoints =
                    Stream.concat(
                                    endpointProvider.orderedStream(),
                                    endpointProviderProvider
                                            .orderedStream()
                                            .flatMap(provider -> provider.getEndpoints().stream()))
                            .toList();
            if (endpoints.isEmpty()) {
                log.warn("[WebSocket] 未注册 WebSocketEndpoint，跳过端点注册");
                return;
            }
            for (var endpoint : endpoints) {
                registry.addHandler(sessionManager, endpoint.path())
                        .setAllowedOriginPatterns(
                                webProperties
                                        .getCors()
                                        .getAllowedOriginPatterns()
                                        .toArray(String[]::new))
                        .addInterceptors(
                                new WebSocketAuthInterceptor(authService, properties, endpoint));
                log.info(
                        "[WebSocket] 端点已注册: path={}, allowedOrigins={}, auth={}",
                        endpoint.path(),
                        webProperties.getCors().getAllowedOriginPatterns(),
                        authService.getClass().getSimpleName());
            }
        }
    }
}
