package com.travis.monolith.system.user.internal.event;

import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.framework.satoken.core.SaTokenWebSocketPrincipal;
import com.travis.infrastructure.framework.websocket.core.WebSocketSessionListener;
import com.travis.monolith.system.user.internal.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * WebSocket 会话生命周期监听器。
 *
 * @author travis
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminWebSocketSessionListener implements WebSocketSessionListener {

    private final SysUserService userService;

    @Override
    public void onConnect(String principal, String ip) {
        log.info("[WebSocket] 用户上线: principal={}", principal);
        var userId = parseAdminUserId(principal);
        if (userId != null) {
            userService.markOnline(userId, ip);
        }
    }

    @Override
    public void onDisconnect(String principal) {
        log.info("[WebSocket] 用户下线: principal={}", principal);
        var userId = parseAdminUserId(principal);
        if (userId != null) {
            userService.markOffline(userId);
        }
    }

    private Long parseAdminUserId(String principal) {
        var subject = SaTokenWebSocketPrincipal.parse(principal);
        if (subject == null || !LoginType.ADMIN.equals(subject.loginType())) {
            return null;
        }
        return Long.valueOf(subject.loginId());
    }
}
