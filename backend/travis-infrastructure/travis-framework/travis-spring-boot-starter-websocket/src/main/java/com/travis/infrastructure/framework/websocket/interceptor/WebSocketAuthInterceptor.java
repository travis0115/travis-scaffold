package com.travis.infrastructure.framework.websocket.interceptor;

import cn.dev33.satoken.stp.StpLogic;
import com.travis.infrastructure.common.web.constant.CommonConstant;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.infrastructure.framework.websocket.core.WebSocketTicketService;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手拦截器，负责认证并将 loginType + userId 存入 Session attributes。
 *
 * <p>前端连接时只需携带短期 ticket，loginType 由服务端注册的 WebSocket 入口绑定。
 *
 * <ul>
 *   <li>{@code ticket} — 由已认证 HTTP 接口签发的一次性握手凭证
 * </ul>
 *
 * <p>示例：{@code ws://host:8080/ws/admin?ticket=xxx}
 *
 * <p>认证成功后将以下属性写入 WebSocketSession 的 attributes：
 *
 * <ul>
 *   <li>{@code loginType} — 登录类型字符串（如 "admin"）
 *   <li>{@code userId} — 用户 ID 字符串
 *   <li>{@code sessionKey} — 复合键 {@code loginType:userId}，作为本地 Session 和 Redis 的唯一标识
 * </ul>
 *
 * @author travis
 */
@Slf4j
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    public static final String ATTR_LOGIN_TYPE = "loginType";
    public static final String ATTR_USER_ID = "userId";
    public static final String ATTR_SESSION_KEY = "sessionKey";
    public static final String ATTR_TOKEN = "token";

    private final String loginType;
    private final WebSocketTicketService ticketService;

    public WebSocketAuthInterceptor(String loginType, WebSocketTicketService ticketService) {
        this.loginType = LoginType.from(loginType);
        if (CommonConstant.UNKNOWN.equals(this.loginType)) {
            throw new IllegalArgumentException("Unsupported WebSocket loginType: " + loginType);
        }
        this.ticketService = ticketService;
    }

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

        String ticket = servletRequest.getServletRequest().getParameter("ticket");
        if (ticket == null || ticket.isBlank()) {
            log.warn(
                    "[WebSocket] 未携带 ticket，拒绝握手: loginType={}, remoteAddr={}",
                    loginType,
                    servletRequest.getServletRequest().getRemoteAddr());
            return false;
        }

        try {
            var webSocketTicket = ticketService.consume(ticket);
            if (webSocketTicket == null) {
                log.warn("[WebSocket] ticket 无效或已过期，拒绝握手: loginType={}", loginType);
                return false;
            }
            if (!loginType.equals(webSocketTicket.loginType())) {
                log.warn(
                        "[WebSocket] ticket loginType 不匹配，拒绝握手: expected={}, actual={}",
                        loginType,
                        webSocketTicket.loginType());
                return false;
            }

            var token = webSocketTicket.token();
            StpLogic stpLogic = StpKit.of(loginType);
            Object loginId = stpLogic.getLoginIdByToken(token);
            if (loginId == null) {
                log.warn("[WebSocket] ticket 绑定的 token 无效或已过期，拒绝握手: loginType={}", loginType);
                return false;
            }

            String userId = loginId.toString();
            if (!userId.equals(webSocketTicket.userId())) {
                log.warn(
                        "[WebSocket] ticket userId 不匹配，拒绝握手: loginType={}, expected={}, actual={}",
                        loginType,
                        webSocketTicket.userId(),
                        userId);
                return false;
            }

            String sessionKey = buildSessionKey(loginType, userId);

            attributes.put(ATTR_LOGIN_TYPE, loginType);
            attributes.put(ATTR_USER_ID, userId);
            attributes.put(ATTR_SESSION_KEY, sessionKey);
            attributes.put(ATTR_TOKEN, token);

            log.debug(
                    "[WebSocket] 握手认证成功: loginType={}, userId={}, sessionKey={}",
                    loginType,
                    userId,
                    sessionKey);
            return true;
        } catch (Exception e) {
            log.warn("[WebSocket] ticket 校验异常，拒绝握手: loginType={}, {}", loginType, e.getMessage());
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

    /**
     * 构建复合键：{@code loginType:userId}
     *
     * @param loginType 登录类型（如 "admin"）
     * @param userId 用户 ID
     * @return 复合键
     */
    public static String buildSessionKey(String loginType, String userId) {
        return loginType + ":" + userId;
    }

}
