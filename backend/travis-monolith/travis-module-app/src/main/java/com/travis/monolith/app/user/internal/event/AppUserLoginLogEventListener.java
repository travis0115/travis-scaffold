package com.travis.monolith.app.user.internal.event;

import com.travis.monolith.app.user.internal.service.AppUserLoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/** 客户端用户登录日志事件监听器。 */
@Component
@RequiredArgsConstructor
public class AppUserLoginLogEventListener {

    private final AppUserLoginLogService loginLogService;

    @ApplicationModuleListener
    void onUserLogin(AppUserLoginEvent event) {
        loginLogService.recordLoginLog(
                event.username(),
                event.status(),
                event.message(),
                event.ip(),
                event.browser(),
                event.os());
    }
}
