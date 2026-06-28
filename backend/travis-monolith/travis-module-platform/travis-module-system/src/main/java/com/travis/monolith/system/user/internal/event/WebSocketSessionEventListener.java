package com.travis.monolith.system.user.internal.event;

import com.travis.infrastructure.common.web.constant.LoginType;
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
public class WebSocketSessionEventListener implements WebSocketSessionListener {

    private final SysUserService userService;

    @Override
    public void onConnect(String loginType, String userId, String ip) {
        log.info("[WebSocket] 用户上线: loginType={}, userId={}", loginType, userId);
        if (LoginType.ADMIN.equals(loginType)) {
            userService.markOnline(Long.valueOf(userId), ip);
        }
    }

    @Override
    public void onDisconnect(String loginType, String userId) {
        log.info("[WebSocket] 用户下线: loginType={}, userId={}", loginType, userId);
        if (LoginType.ADMIN.equals(loginType)) {
            userService.markOffline(Long.valueOf(userId));
        }
    }
}
