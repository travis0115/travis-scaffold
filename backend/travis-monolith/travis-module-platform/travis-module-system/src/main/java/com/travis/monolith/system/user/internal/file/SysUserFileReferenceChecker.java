package com.travis.monolith.system.user.internal.file;

import com.travis.monolith.system.file.api.SysFileReferenceChecker;
import com.travis.monolith.system.user.internal.entity.SysUser;
import com.travis.monolith.system.user.internal.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 检查文件是否被后台用户头像引用。 */
@Component
@RequiredArgsConstructor
public class SysUserFileReferenceChecker implements SysFileReferenceChecker {

    private final SysUserService userService;

    @Override
    public boolean isReferenced(Long fileId) {
        return userService.lambdaQuery().eq(SysUser::getAvatarFileId, fileId).exists();
    }
}
