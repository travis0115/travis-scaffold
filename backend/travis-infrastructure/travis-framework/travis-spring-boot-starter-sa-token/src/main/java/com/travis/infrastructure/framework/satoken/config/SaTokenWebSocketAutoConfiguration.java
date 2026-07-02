package com.travis.infrastructure.framework.satoken.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.hutool.core.util.StrUtil;
import com.travis.infrastructure.framework.satoken.config.properties.SaTokenProperties;
import com.travis.infrastructure.framework.satoken.core.websocket.ticket.DefaultSaTokenWebSocketTicketStore;
import com.travis.infrastructure.framework.satoken.core.websocket.SaTokenWebSocketAuthService;
import com.travis.infrastructure.framework.satoken.core.websocket.SaTokenWebSocketSubjectValidator;
import com.travis.infrastructure.framework.satoken.core.websocket.ticket.SaTokenWebSocketTicketStore;
import com.travis.infrastructure.framework.websocket.config.WebSocketAutoConfiguration;
import com.travis.infrastructure.framework.websocket.core.auth.WebSocketAuthService;
import com.travis.infrastructure.framework.websocket.core.endpoint.WebSocketEndpoint;
import com.travis.infrastructure.framework.websocket.core.endpoint.WebSocketEndpointProvider;
import com.travis.infrastructure.framework.websocket.core.session.WebSocketNamespace;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.Map;

/** Sa-Token 对 WebSocket starter 的认证适配。 */
@AutoConfiguration(after = {SaTokenAutoConfiguration.class, WebSocketAutoConfiguration.class})
@ConditionalOnClass(WebSocketAuthService.class)
public class SaTokenWebSocketAutoConfiguration {

    /** WebSocket ticket 存储，复用 Sa-Token 当前 Dao，因此自动跟随 Sa-Token 使用内存或 Redis。 */
    @Bean
    @ConditionalOnBean(SaTokenDao.class)
    @ConditionalOnMissingBean(SaTokenWebSocketTicketStore.class)
    public SaTokenWebSocketTicketStore saTokenWebSocketTicketStore(
            SaTokenDao saTokenDao, Environment environment) {
        var tokenName = environment.getProperty("sa-token.token-name", "Authorization");
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
                        .filter(rule -> StrUtil.isNotBlank(rule.getWebsocketPath()))
                        .map(
                                rule ->
                                        new WebSocketEndpoint(
                                                normalizePath(rule.getWebsocketPath()),
                                                Map.of(
                                                        SaTokenWebSocketAuthService.ATTR_LOGIN_TYPE,
                                                        rule.getLoginType(),
                                                        WebSocketNamespace.ATTR_NAMESPACE,
                                                        rule.getLoginType())))
                        .toList();
    }

    private String normalizePath(String path) {
        var normalized = path.trim();
        return normalized.startsWith("/") ? normalized : "/" + normalized;
    }
}
