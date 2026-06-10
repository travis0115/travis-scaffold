package com.travis.monolith.system.log.loginlog.internal.event.consumer;

import com.travis.infrastructure.framework.rocketmq.core.AbstractEventListener;
import com.travis.monolith.system.common.api.SystemEventConstant;
import com.travis.monolith.system.log.loginlog.internal.service.SysLoginLogService;
import com.travis.monolith.system.user.api.event.UserLoginPayload;
import com.travis.monolith.system.user.internal.event.EventConsumerGroup;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.client.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

/**
 * 登录日志事件消费者，通过 RocketMQ 接收 {@link UserLoginPayload} 并调用登录日志服务进行持久化。
 *
 * @author travis
 */
@Component
@RocketMQMessageListener(
        topic = SystemEventConstant.NORMAL_TOPIC,
        tag = SystemEventConstant.USER_LOGIN_TAG,
        consumerGroup = EventConsumerGroup.USER_LOGIN_CONSUMER_GROUP)
@RequiredArgsConstructor
public class LoginLogEventListener extends AbstractEventListener<UserLoginPayload> {

    private final SysLoginLogService loginLogService;

    @Override
    protected void onEvent(UserLoginPayload payload) {
        loginLogService.recordLoginLog(
                payload.username(),
                payload.status(),
                payload.message(),
                payload.ip(),
                payload.browser(),
                payload.os());
    }
}
