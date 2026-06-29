package com.travis.infrastructure.framework.satoken.config;

import cn.dev33.satoken.dao.SaTokenDao;
import com.travis.infrastructure.framework.satoken.config.properties.SaTokenProperties;
import com.travis.infrastructure.framework.satoken.core.DefaultSaTokenWebSocketTicketStore;
import com.travis.infrastructure.framework.satoken.core.SaTokenWebSocketAuthService;
import com.travis.infrastructure.framework.satoken.core.SaTokenWebSocketSubjectValidator;
import com.travis.infrastructure.framework.satoken.core.SaTokenWebSocketTicketStore;
import com.travis.infrastructure.framework.websocket.core.WebSocketAuthService;
import com.travis.infrastructure.framework.websocket.core.WebSocketEndpoint;
import com.travis.infrastructure.framework.websocket.core.WebSocketEndpointProvider;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/** Sa-Token 对 WebSocket starter 的认证适配。 */
@AutoConfiguration(
        after = TravisSaTokenAutoConfiguration.class,
        afterName =
                "com.travis.infrastructure.framework.websocket.config.TravisWebSocketAutoConfiguration")
@ConditionalOnClass(WebSocketAuthService.class)
public class SaTokenWebSocketAutoConfiguration {

    /** WebSocket ticket 存储，复用 Sa-Token 当前 Dao，因此自动跟随 Sa-Token 使用内存或 Redis。 */
    @Bean
    @ConditionalOnBean(SaTokenDao.class)
    @ConditionalOnMissingBean(SaTokenWebSocketTicketStore.class)
    public SaTokenWebSocketTicketStore saTokenWebSocketTicketStore(
            SaTokenDao saTokenDao, Environment environment) {
        var tokenName = environment.getProperty("sa-token.token-name", "satoken");
        return new DefaultSaTokenWebSocketTicketStore(saTokenDao, tokenName);
    }

    @Bean
    @ConditionalOnBean(SaTokenWebSocketTicketStore.class)
    @ConditionalOnMissingBean(WebSocketAuthService.class)
    public WebSocketAuthService saTokenWebSocketAuthService(
            SaTokenWebSocketTicketStore ticketStore,
            List<SaTokenWebSocketSubjectValidator> subjectValidators) {
        return new SaTokenWebSocketAuthService(ticketStore, subjectValidators);
    }

    @Bean
    public WebSocketEndpointProvider saTokenWebSocketEndpointProvider(
            SaTokenProperties saTokenProperties) {
        return () ->
                saTokenProperties.getAuthRules().stream()
                        .filter(rule -> rule.getWebsocketPath() != null)
                        .filter(rule -> !rule.getWebsocketPath().isBlank())
                        .map(
                                rule ->
                                        new WebSocketEndpoint(
                                                normalizePath(rule.getWebsocketPath()),
                                                Map.of(
                                                        SaTokenWebSocketAuthService.ATTR_LOGIN_TYPE,
                                                        rule.getLoginType())))
                        .toList();
    }

    private String normalizePath(String path) {
        var normalized = path.trim();
        return normalized.startsWith("/") ? normalized : "/" + normalized;
    }
}
