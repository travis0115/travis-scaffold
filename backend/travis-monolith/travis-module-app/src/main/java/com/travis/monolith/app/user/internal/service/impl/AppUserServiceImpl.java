package com.travis.monolith.app.user.internal.service.impl;

import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.app.user.api.response.AppUserOptionResp;
import com.travis.monolith.app.user.internal.entity.AppUser;
import com.travis.monolith.app.user.internal.mapper.AppUserMapper;
import com.travis.monolith.app.user.internal.service.AppUserService;
import com.travis.monolith.system.common.api.enums.Status;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AppUserServiceImpl extends ServiceImplX<AppUserMapper, AppUser>
        implements AppUserService {

    @Override
    public List<AppUserOptionResp> listOptions(String keyword, int limit) {
        var wrapper = baseOptionWrapper();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(
                    condition ->
                            condition
                                    .like(AppUser::getUsername, keyword)
                                    .or()
                                    .like(AppUser::getNickname, keyword)
                                    .or()
                                    .like(AppUser::getMobile, keyword));
        }
        wrapper.orderByDesc(AppUser::getCreateTime).last("LIMIT " + Math.clamp(limit, 1, 50));
        return toOptions(list(wrapper));
    }

    @Override
    public List<AppUserOptionResp> listOptionsByIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return toOptions(list(baseOptionWrapper().in(AppUser::getId, userIds)));
    }

    private LambdaQueryWrapperX<AppUser> baseOptionWrapper() {
        return new LambdaQueryWrapperX<AppUser>().eq(AppUser::getStatus, Status.ENABLED.getValue());
    }

    private List<AppUserOptionResp> toOptions(List<AppUser> users) {
        return users.stream()
                .map(
                        user ->
                                new AppUserOptionResp(
                                        user.getId(),
                                        user.getUsername(),
                                        user.getNickname(),
                                        user.getMobile()))
                .toList();
    }
}
