package com.travis.monolith.app.user.internal.file;

import com.travis.monolith.app.user.internal.entity.AppUser;
import com.travis.monolith.app.user.internal.service.AppUserService;
import com.travis.monolith.system.file.api.SysFileReferenceChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 检查文件是否被客户端用户头像引用。 */
@Component
@RequiredArgsConstructor
public class AppUserFileReferenceChecker implements SysFileReferenceChecker {

    private final AppUserService userService;

    @Override
    public boolean isReferenced(Long fileId) {
        return userService.lambdaQuery().eq(AppUser::getAvatarFileId, fileId).exists();
    }
}
