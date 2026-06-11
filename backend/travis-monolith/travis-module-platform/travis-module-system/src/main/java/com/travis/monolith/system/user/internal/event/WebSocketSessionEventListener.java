package com.travis.monolith.system.user.internal.event;

import com.travis.infrastructure.common.event.MessagePublisher;
import com.travis.infrastructure.framework.websocket.core.WebSocketSessionListener;
import com.travis.monolith.system.common.api.SystemEvent;
import com.travis.monolith.system.user.api.event.UserOnlinePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * WebSocket 会话生命周期监听器，在用户首次连接或最后断开时发布 RocketMQ 事件。
 *
 * <p>集群部署下，每个实例只对自己上面的连接触发回调，通过 RocketMQ 广播到所有实例。
 *
 * @author travis
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketSessionEventListener implements WebSocketSessionListener {

    private final MessagePublisher messagePublisher;

    @Override
    public void onConnect(String loginType, String userId) {
        log.info("[WebSocket] 用户上线: loginType={}, userId={}", loginType, userId);
        messagePublisher.publish(
                SystemEvent.USER_ONLINE_STATUS,
                UserOnlinePayload.builder()
                        .loginType(loginType)
                        .userId(userId)
                        .online(true)
                        .build());
    }

    @Override
    public void onDisconnect(String loginType, String userId) {
        log.info("[WebSocket] 用户下线: loginType={}, userId={}", loginType, userId);
        messagePublisher.publish(
                SystemEvent.USER_ONLINE_STATUS,
                UserOnlinePayload.builder()
                        .loginType(loginType)
                        .userId(userId)
                        .online(false)
                        .build());
    }
}
