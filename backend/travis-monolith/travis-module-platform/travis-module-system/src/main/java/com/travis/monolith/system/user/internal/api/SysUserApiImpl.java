package com.travis.monolith.system.user.internal.api;

import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.monolith.system.user.api.SysUserApi;
import com.travis.monolith.system.user.internal.entity.SysUser;
import com.travis.monolith.system.user.internal.service.SysUserService;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SysUserApiImpl implements SysUserApi {
    private final SysUserService userService;

    @Override
    public List<Long> listEnabledUserIds() {
        return queryEnabledUserIds(new LambdaQueryWrapperX<>());
    }

    @Override
    public List<Long> listEnabledUserIdsByIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return queryEnabledUserIds(new LambdaQueryWrapperX<SysUser>().in(SysUser::getId, userIds));
    }

    @Override
    public List<Long> listEnabledUserIdsByDeptIds(Collection<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return List.of();
        }
        return queryEnabledUserIds(
                new LambdaQueryWrapperX<SysUser>().in(SysUser::getDeptId, deptIds));
    }

    private List<Long> queryEnabledUserIds(LambdaQueryWrapperX<SysUser> wrapper) {
        return userService.list(wrapper.eq(SysUser::getStatus, 1)).stream()
                .map(SysUser::getId)
                .toList();
    }
}
