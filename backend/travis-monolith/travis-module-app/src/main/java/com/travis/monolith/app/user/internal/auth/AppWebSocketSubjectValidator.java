package com.travis.monolith.app.user.internal.auth;

import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.framework.satoken.core.websocket.SaTokenWebSocketSubjectValidator;
import com.travis.monolith.app.user.internal.service.AppUserService;
import com.travis.monolith.system.common.api.enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppWebSocketSubjectValidator implements SaTokenWebSocketSubjectValidator {

    private final AppUserService userService;

    @Override
    public boolean supports(String loginType) {
        return LoginType.APP.equals(loginType);
    }

    @Override
    public boolean isValid(String loginType, String loginId) {
        var user = userService.getById(Long.valueOf(loginId));
        return user != null && !Status.DISABLED.getValue().equals(user.getStatus());
    }
}
