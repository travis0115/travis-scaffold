package com.travis.monolith.system.user.internal.event;

import com.travis.infrastructure.framework.websocket.core.WebSocketSessionListener;
import com.travis.monolith.system.user.api.event.UserOnlinePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * WebSocket 会话生命周期监听器，在用户首次连接或最后断开时发布应用事件。
 *
 * @author travis
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketSessionEventListener implements WebSocketSessionListener {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void onConnect(String loginType, String userId) {
        log.info("[WebSocket] 用户上线: loginType={}, userId={}", loginType, userId);
        eventPublisher.publishEvent(
                UserOnlinePayload.builder()
                        .loginType(loginType)
                        .userId(userId)
                        .isOnline(true)
                        .build());
    }

    @Override
    public void onDisconnect(String loginType, String userId) {
        log.info("[WebSocket] 用户下线: loginType={}, userId={}", loginType, userId);
        eventPublisher.publishEvent(
                UserOnlinePayload.builder()
                        .loginType(loginType)
                        .userId(userId)
                        .isOnline(false)
                        .build());
    }
}
