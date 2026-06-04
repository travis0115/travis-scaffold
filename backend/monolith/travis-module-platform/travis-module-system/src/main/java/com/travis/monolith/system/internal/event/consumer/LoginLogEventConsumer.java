package com.travis.monolith.system.internal.event.consumer;

import com.travis.monolith.system.internal.event.LoginLogEvent;
import com.travis.monolith.system.internal.service.SysLoginLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 登录日志事件消费者，接收 {@link LoginLogEvent} 并调用登录日志服务进行持久化。
 * <p>
 * 日志持久化使用独立事务（REQUIRES_NEW），确保登录失败时外层事务回滚不影响日志记录。
 *
 * @author travis
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginLogEventConsumer {

    private final SysLoginLogService loginLogService;

    @EventListener
    public void onEvent(LoginLogEvent event) {
        loginLogService.recordLoginLog(event.getUsername(), event.getStatus(), event.getMessage());
    }
}
