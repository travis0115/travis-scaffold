package com.travis.monolith.system.common.api;

import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.monolith.system.common.api.enums.IsBuiltin;
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import org.springframework.stereotype.Component;

@Component
public class BuiltinResourceGuard {

    private static final String ADMIN_ROLE_CODE = "admin";

    public void checkUpdate(Integer isBuiltin) {
        if (!IsBuiltin.YES.getValue().equals(isBuiltin)) {
            return;
        }
        if (!StpKit.of(LoginType.ADMIN).hasRole(ADMIN_ROLE_CODE)) {
            throw new BizException(SystemErrorCode.ROLE_ADMIN_BUILTIN_MODIFIABLE_ONLY);
        }
    }
}
