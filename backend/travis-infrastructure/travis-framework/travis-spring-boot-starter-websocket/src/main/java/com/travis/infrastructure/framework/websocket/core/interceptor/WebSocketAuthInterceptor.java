package com.travis.infrastructure.framework.websocket.core.interceptor;

import com.travis.infrastructure.framework.websocket.config.properties.WebSocketProperties;
import com.travis.infrastructure.framework.websocket.core.auth.WebSocketAuthRequest;
import com.travis.infrastructure.framework.websocket.core.auth.WebSocketAuthService;
import com.travis.infrastructure.framework.websocket.core.endpoint.WebSocketEndpoint;
import com.travis.infrastructure.framework.websocket.core.session.WebSocketPrincipal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * WebSocket 握手拦截器，负责认证并将连接主体写入 Session attributes。
 *
 * <p>前端连接时需携带认证适配器约定的握手凭证。
 *
 * <ul>
 *   <li>{@code ticket} — 默认参数名，可通过 {@code travis.web.websocket.credential-key} 调整
 * </ul>
 *
 * <p>示例：{@code ws://host:8080/ws/admin?ticket=xxx}
 *
 * <p>认证成功后将以下属性写入 WebSocketSession 的 attributes：
 *
 * <ul>
 *   <li>{@code principal} — 连接主体标识，作为本地 Session 和 Redis 的唯一标识
 * </ul>
 *
 * @author travis
 */
@Slf4j
@AllArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final WebSocketAuthService authService;
    private final WebSocketProperties properties;
    private final WebSocketEndpoint endpoint;

    @Override
    public boolean beforeHandshake(
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            @NonNull Map<String, Object> attributes) {

        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            log.warn("[WebSocket] 非 Servlet 请求，拒绝握手");
            return false;
        }

        String credential =
                servletRequest.getServletRequest().getParameter(properties.getCredentialKey());
        if (credential == null || credential.isBlank()) {
            log.warn(
                    "[WebSocket] 未携带握手凭证，拒绝握手: parameter={}, remoteAddr={}",
                    properties.getCredentialKey(),
                    servletRequest.getServletRequest().getRemoteAddr());
            return false;
        }

        try {
            var authContext =
                    authService.authenticate(
                            new WebSocketAuthRequest(
                                    servletRequest.getServletRequest().getRequestURI(),
                                    credential,
                                    request.getHeaders(),
                                    servletRequest.getServletRequest().getRemoteAddr(),
                                    endpoint.attributes()));
            if (authContext == null) {
                log.warn("[WebSocket] 握手凭证无效或已过期，拒绝握手");
                return false;
            }
            String principal = authContext.principal();
            if (principal == null || principal.isBlank()) {
                log.warn("[WebSocket] principal 为空，拒绝握手");
                return false;
            }

            attributes.putAll(endpoint.attributes());
            attributes.put(WebSocketPrincipal.ATTR_PRINCIPAL, principal);
            attributes.putAll(authContext.attributes());

            log.debug("[WebSocket] 握手认证成功: principal={}", principal);
            return true;
        } catch (Exception e) {
            log.warn("[WebSocket] 握手凭证校验异常，拒绝握手: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            Exception exception) {
        // 握手完成，无需处理
    }
}
