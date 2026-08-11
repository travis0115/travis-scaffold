package com.travis.monolith.app.user.internal.file;

import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.monolith.app.user.internal.entity.AppUser;
import com.travis.monolith.app.user.internal.service.AppUserService;
import com.travis.monolith.system.file.api.SysFileUploaderNameResolver;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 批量解析客户端用户上传主体名称。 */
@Component
@RequiredArgsConstructor
public class AppUserFileUploaderNameResolver implements SysFileUploaderNameResolver {

    private final AppUserService userService;

    @Override
    public String getUploaderType() {
        return LoginType.APP;
    }

    @Override
    public Map<Long, String> resolveNames(Collection<Long> uploaderIds) {
        if (uploaderIds == null || uploaderIds.isEmpty()) {
            return Map.of();
        }
        return userService
                .list(
                        new LambdaQueryWrapperX<AppUser>()
                                .select(AppUser::getId, AppUser::getUsername)
                                .in(AppUser::getId, uploaderIds))
                .stream()
                .collect(
                        Collectors.toMap(
                                AppUser::getId,
                                AppUser::getUsername,
                                (left, right) -> left,
                                LinkedHashMap::new));
    }
}
