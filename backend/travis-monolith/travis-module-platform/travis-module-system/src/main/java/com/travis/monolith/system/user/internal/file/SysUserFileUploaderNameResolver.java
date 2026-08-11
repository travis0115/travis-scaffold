package com.travis.monolith.system.user.internal.file;

import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.monolith.system.file.api.SysFileUploaderNameResolver;
import com.travis.monolith.system.user.internal.service.SysUserService;
import java.util.Collection;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 批量解析后台用户上传主体名称。 */
@Component
@RequiredArgsConstructor
public class SysUserFileUploaderNameResolver implements SysFileUploaderNameResolver {

    private final SysUserService userService;

    @Override
    public String getUploaderType() {
        return LoginType.ADMIN;
    }

    @Override
    public Map<Long, String> resolveNames(Collection<Long> uploaderIds) {
        return userService.getUsernameMapByIds(uploaderIds);
    }
}
