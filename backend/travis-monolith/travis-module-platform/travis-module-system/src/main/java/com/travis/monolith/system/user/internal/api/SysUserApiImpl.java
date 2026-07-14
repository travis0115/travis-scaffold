package com.travis.monolith.system.user.internal.api;

import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.dept.api.SysDeptApi;
import com.travis.monolith.system.user.api.SysUserApi;
import com.travis.monolith.system.user.api.response.SysUserOptionResp;
import com.travis.monolith.system.user.api.response.SysUserResp;
import com.travis.monolith.system.user.internal.entity.SysUser;
import com.travis.monolith.system.user.internal.service.SysUserService;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SysUserApiImpl implements SysUserApi {
    private final SysUserService userService;
    private final SysDeptApi deptApi;

    @Override
    public List<Long> listUserIds() {
        return userService.listUserIds();
    }

    @Override
    public List<Long> listUserIdsByDeptIds(Collection<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return List.of();
        }
        var userIdList = new ArrayList<Long>();
        deptIds.forEach(id -> userIdList.addAll(userService.listUserIdsByDeptId(id)));
        return userIdList;
    }

    @Override
    public Long getDeptIdByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        SysUserResp user;
        try {
            user = userService.getDetailByIdOrThrow(userId);
        } catch (Exception e) {
            return null;
        }
        return user.getDeptId();
    }

    @Override
    public String getUsernameById(Long userId) {
        return userService.getUsernameById(userId);
    }

    // TODO 调整API
    @Override
    public Map<Long, String> getUsernameMapByIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> result = new LinkedHashMap<>();
        for (Long userId : userIds) {
            var username = userService.getUsernameById(userId);
            if (username != null) {
                result.put(userId, username);
            }
        }
        return result;
    }

    @Override
    public List<SysUserOptionResp> listCurrentUserScopedOptions(String keyword, int limit) {
        var wrapper = currentUserScopeWrapper();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(
                    condition ->
                            condition
                                    .like(SysUser::getUsername, keyword)
                                    .or()
                                    .like(SysUser::getNickname, keyword)
                                    .or()
                                    .like(SysUser::getMobile, keyword)
                                    .or()
                                    .like(SysUser::getEmail, keyword));
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
        var currentUserId = StpKit.of(LoginType.ADMIN).getLoginIdAsLong();
        var currentUser = userService.getDetailByIdOrThrow(currentUserId);
        var wrapper =
                new LambdaQueryWrapperX<SysUser>()
                        .eq(SysUser::getStatus, Status.ENABLED.getValue());
        if (currentUser.getDeptId() == null) {
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
