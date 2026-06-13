package com.travis.monolith.system.user.internal.api;

import com.travis.infrastructure.common.web.enums.LoginType;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.monolith.system.dept.api.SysDeptApi;
import com.travis.monolith.system.user.api.SysUserApi;
import com.travis.monolith.system.user.api.response.SysUserOptionResp;
import com.travis.monolith.system.user.internal.entity.SysUser;
import com.travis.monolith.system.user.internal.mapper.SysUserMapper;
import com.travis.monolith.system.user.internal.service.SysUserService;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SysUserApiImpl implements SysUserApi {
    private final SysUserService userService;
    private final SysUserMapper userMapper;
    private final SysDeptApi deptApi;

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

    @Override
    public String getUsernameById(Long userId) {
        if (userId == null) {
            return null;
        }
        var user = userMapper.selectById(userId);
        return user != null ? user.getUsername() : null;
    }

    @Override
    public Map<Long, String> getUsernameMapByIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getUsername));
    }

    @Override
    public List<SysUserOptionResp> listCurrentUserScopedOptions(String keyword, int limit) {
        LambdaQueryWrapperX<SysUser> wrapper = currentUserScopeWrapper();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(
                    condition ->
                            condition
                                    .like(SysUser::getUsername, keyword)
                                    .or()
                                    .like(SysUser::getNickname, keyword)
                                    .or()
                                    .like(SysUser::getMobile, keyword));
        }
        wrapper.orderByAsc(SysUser::getUsername).last("LIMIT " + Math.clamp(limit, 1, 50));
        return toOptions(userService.list(wrapper));
    }

    @Override
    public List<SysUserOptionResp> listCurrentUserScopedOptionsByIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return toOptions(userService.list(currentUserScopeWrapper().in(SysUser::getId, userIds)));
    }

    private LambdaQueryWrapperX<SysUser> currentUserScopeWrapper() {
        long currentUserId = StpKit.of(LoginType.ADMIN).getLoginIdAsLong();
        SysUser currentUser =
                userService
                        .lambdaQuery()
                        .select(SysUser::getId, SysUser::getDeptId)
                        .eq(SysUser::getId, currentUserId)
                        .one();
        LambdaQueryWrapperX<SysUser> wrapper =
                new LambdaQueryWrapperX<SysUser>().eq(SysUser::getStatus, 1);
        if (currentUser == null || currentUser.getDeptId() == null) {
            return wrapper.eq(SysUser::getId, currentUserId);
        }
        return wrapper.in(
                SysUser::getDeptId, deptApi.listSelfAndDescendantIds(currentUser.getDeptId()));
    }

    private List<SysUserOptionResp> toOptions(List<SysUser> users) {
        Set<Long> deptIds =
                users.stream()
                        .map(SysUser::getDeptId)
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toSet());
        Map<Long, String> deptNames = deptApi.getDeptNameMapByIds(deptIds);
        return users.stream()
                .map(
                        user ->
                                new SysUserOptionResp(
                                        user.getId(),
                                        user.getUsername(),
                                        user.getNickname(),
                                        deptNames.get(user.getDeptId())))
                .toList();
    }
}
