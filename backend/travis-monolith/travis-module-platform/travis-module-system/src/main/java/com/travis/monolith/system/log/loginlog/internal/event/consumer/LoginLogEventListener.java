package com.travis.monolith.system.log.loginlog.internal.event.consumer;

import com.travis.monolith.system.log.loginlog.internal.service.SysLoginLogService;
import com.travis.monolith.system.user.api.event.UserLoginPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * 登录日志事件监听器，接收 {@link UserLoginPayload} 并调用登录日志服务进行持久化。
 *
 * @author travis
 */
@Component
@RequiredArgsConstructor
public class LoginLogEventListener {

    private final SysLoginLogService loginLogService;

    @ApplicationModuleListener
    void onUserLogin(UserLoginPayload payload) {
        loginLogService.recordLoginLog(
                payload.username(),
                payload.status(),
                payload.message(),
                payload.ip(),
                payload.browser(),
                payload.os());
    }
}
