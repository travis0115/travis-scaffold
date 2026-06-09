package com.travis.monolith.system.log.loginlog.internal.event.consumer;

import com.travis.infrastructure.framework.rocketmq.core.AbstractEventConsumer;
import com.travis.monolith.system.log.loginlog.internal.service.SysLoginLogService;
import com.travis.monolith.system.user.api.event.UserLoginEvent;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.client.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;

/**
 * 登录日志事件消费者，通过 RocketMQ 接收 {@link UserLoginEvent} 并调用登录日志服务进行持久化。
 *
 * <p>日志持久化使用独立事务（REQUIRES_NEW），确保登录失败时外层事务回滚不影响日志记录。
 *
 * @author travis
 */
@Component
@RocketMQMessageListener(
        topic = "system-event",
        tag = "user-login",
        consumerGroup = "system-user-login-consumer")
@RequiredArgsConstructor
public class LoginLogEventConsumer extends AbstractEventConsumer<UserLoginEvent> {

    private final SysLoginLogService loginLogService;

    @Override
    protected void onEvent(UserLoginEvent payload) {
        loginLogService.recordLoginLog(
                payload.getUsername(), payload.getStatus(), payload.getMessage());
    }
}
