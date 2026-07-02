package com.travis.monolith.system.user.internal.auth;

import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.framework.satoken.core.websocket.SaTokenWebSocketSubjectValidator;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.user.internal.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 管理后台 WebSocket 账号状态校验。 */
@Component
@RequiredArgsConstructor
public class AdminWebSocketSubjectValidator implements SaTokenWebSocketSubjectValidator {

    private final SysUserService userService;

    @Override
    public boolean supports(String loginType) {
        return LoginType.ADMIN.equals(loginType);
    }

    @Override
    public boolean isValid(String loginType, String loginId) {
        var user = userService.getById(Long.valueOf(loginId));
        return user != null && !Status.DISABLED.getValue().equals(user.getStatus());
    }
}
