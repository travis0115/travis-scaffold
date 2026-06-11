package com.travis.infrastructure.framework.websocket.config;

import com.travis.infrastructure.framework.websocket.core.LocalWebSocketSessionManager;
import com.travis.infrastructure.framework.websocket.core.RedisWebSocketMessageDispatcher;
import com.travis.infrastructure.framework.websocket.core.WebSocketMessageSender;
import com.travis.infrastructure.framework.websocket.core.WebSocketSessionListener;
import com.travis.infrastructure.framework.websocket.core.WebSocketSessionManager;
import com.travis.infrastructure.framework.websocket.interceptor.WebSocketAuthInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

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

    /** 认证拦截器（需 Sa-Token 在 classpath 上） */
    @Bean
    @ConditionalOnClass(name = "cn.dev33.satoken.stp.StpUtil")
    @ConditionalOnMissingBean
    public WebSocketAuthInterceptor webSocketAuthInterceptor() {
        return new WebSocketAuthInterceptor();
    }

    /** Redis 消息监听容器 */
    @Bean
    @ConditionalOnMissingBean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory) {
        var container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

    /**
     * Redis 消息分发器。
     *
     * <p>通过 setter 注入 {@link LocalWebSocketSessionManager} 来打破循环依赖。
     */
    @Bean
    public RedisWebSocketMessageDispatcher redisWebSocketMessageDispatcher(
            RedisTemplate<String, Object> redisTemplate,
            RedisMessageListenerContainer listenerContainer,
            WebSocketProperties properties) {
        var dispatcher = new RedisWebSocketMessageDispatcher(redisTemplate, properties);
        dispatcher.subscribe(listenerContainer);
        return dispatcher;
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
    static class WebSocketEndpointConfigurer implements WebSocketConfigurer {

        private final WebSocketProperties properties;
        private final LocalWebSocketSessionManager sessionManager;
        private final ObjectProvider<WebSocketAuthInterceptor> authInterceptorProvider;

        public WebSocketEndpointConfigurer(
                WebSocketProperties properties,
                LocalWebSocketSessionManager sessionManager,
                ObjectProvider<WebSocketAuthInterceptor> authInterceptorProvider) {
            this.properties = properties;
            this.sessionManager = sessionManager;
            this.authInterceptorProvider = authInterceptorProvider;
        }

        @Override
        public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
            var handlers =
                    registry.addHandler(sessionManager, properties.getPath())
                            .setAllowedOrigins(properties.getAllowedOrigins());

            // 认证拦截器可选（Sa-Token 未引入时不存在）
            var authInterceptor = authInterceptorProvider.getIfAvailable();
            if (authInterceptor != null) {
                handlers.addInterceptors(authInterceptor);
            }

            log.info(
                    "[WebSocket] 端点已注册: path={}, allowedOrigins={}, auth={}",
                    properties.getPath(),
                    properties.getAllowedOrigins(),
                    authInterceptor != null ? "Sa-Token" : "无");
        }
    }
}
