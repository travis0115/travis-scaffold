package com.travis.monolith.system.common.api;

import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.monolith.system.common.api.enums.IsBuiltin;
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import org.springframework.stereotype.Component;

/** 校验系统内置资源是否只能由超级管理员修改。 */
@Component
public class BuiltinResourceGuard {

    /** 拥有内置资源修改权限的角色编码。 */
    private static final String ADMIN_ROLE_CODE = "admin";

    /** 当资源为系统内置时，校验当前用户是否拥有超级管理员角色。 */
    public void checkUpdate(Integer isBuiltin) {
        if (!IsBuiltin.YES.getValue().equals(isBuiltin)) {
            return;
        }
        if (!StpKit.of(LoginType.ADMIN).hasRole(ADMIN_ROLE_CODE)) {
            throw new BizException(SystemErrorCode.ROLE_ADMIN_BUILTIN_MODIFIABLE_ONLY);
        }
    }
}
