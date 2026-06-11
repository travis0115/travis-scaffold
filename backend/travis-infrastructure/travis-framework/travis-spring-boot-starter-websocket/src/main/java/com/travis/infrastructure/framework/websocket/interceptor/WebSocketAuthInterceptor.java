package com.travis.infrastructure.framework.websocket.interceptor;

import cn.dev33.satoken.stp.StpLogic;
import com.travis.infrastructure.common.web.enums.LoginType;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * WebSocket 握手拦截器，负责认证并将 loginType + userId 存入 Session attributes。
 *
 * <p>前端连接时需携带两个 query param：
 *
 * <ul>
 *   <li>{@code token} — Sa-Token 凭证
 *   <li>{@code loginType} — 登录类型（admin / user），对应 {@link LoginType} 枚举
 * </ul>
 *
 * <p>示例：{@code ws://host:8080/ws?token=xxx&loginType=admin}
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

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {

        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            log.warn("[WebSocket] 非 Servlet 请求，拒绝握手");
            return false;
        }

        // 提取 loginType
        String loginTypeStr = servletRequest.getServletRequest().getParameter("loginType");
        LoginType loginType = LoginType.from(loginTypeStr);
        if (loginType == LoginType.UNKNOWN) {
            log.warn("[WebSocket] loginType 无效，拒绝握手: loginType={}", loginTypeStr);
            return false;
        }

        // 提取 token
        String token = extractToken(servletRequest);
        if (token == null || token.isBlank()) {
            log.warn(
                    "[WebSocket] 未携带 token，拒绝握手: loginType={}, remoteAddr={}",
                    loginType.getCode(),
                    servletRequest.getServletRequest().getRemoteAddr());
            return false;
        }

        try {
            // 通过对应 loginType 的 StpLogic 校验 token 并获取 userId
            StpLogic stpLogic = StpKit.of(loginType);
            Object loginId = stpLogic.getLoginIdByToken(token);
            if (loginId == null) {
                log.warn("[WebSocket] token 无效或已过期，拒绝握手: loginType={}", loginType.getCode());
                return false;
            }

            String userId = loginId.toString();
            String sessionKey = buildSessionKey(loginType.getCode(), userId);

            attributes.put("loginType", loginType.getCode());
            attributes.put("userId", userId);
            attributes.put("sessionKey", sessionKey);

            log.debug(
                    "[WebSocket] 握手认证成功: loginType={}, userId={}, sessionKey={}",
                    loginType.getCode(),
                    userId,
                    sessionKey);
            return true;
        } catch (Exception e) {
            log.warn(
                    "[WebSocket] token 校验异常，拒绝握手: loginType={}, {}",
                    loginType.getCode(),
                    e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
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

    /** 从 query param 或 Authorization header 中提取 token */
    private String extractToken(ServletServerHttpRequest request) {
        // 1. 从 query param 提取
        String token = request.getServletRequest().getParameter("token");
        if (token != null && !token.isBlank()) {
            return token.trim();
        }

        // 2. 从 Authorization header 提取
        String authHeader = request.getServletRequest().getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }

        return null;
    }
}
