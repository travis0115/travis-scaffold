package com.travis.monolith.system.log.loginlog.internal.event.consumer;

import com.travis.infrastructure.framework.rocketmq.core.AbstractEventConsumer;
import com.travis.monolith.system.common.api.SystemEvent;
import com.travis.monolith.system.log.loginlog.internal.service.SysLoginLogService;
import com.travis.monolith.system.user.api.event.UserLoginPayload;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.client.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

/**
 * 登录日志事件消费者，通过 RocketMQ 接收 {@link UserLoginPayload} 并调用登录日志服务进行持久化。
 *
 * <p>日志持久化使用独立事务（REQUIRES_NEW），确保登录失败时外层事务回滚不影响日志记录。
 *
 * @author travis
 */
@Component
@RocketMQMessageListener(
        topic = SystemEvent.TOPIC,
        tag = SystemEvent.USER_LOGIN_TAG,
        consumerGroup = SystemEvent.USER_LOGIN_GROUP)
@RequiredArgsConstructor
public class LoginLogEventConsumer extends AbstractEventConsumer<UserLoginPayload> {

    private final SysLoginLogService loginLogService;

    @Override
    protected void onEvent(UserLoginPayload payload) {
        loginLogService.recordLoginLog(
                payload.getUsername(), payload.getStatus(), payload.getMessage());
    }
}
