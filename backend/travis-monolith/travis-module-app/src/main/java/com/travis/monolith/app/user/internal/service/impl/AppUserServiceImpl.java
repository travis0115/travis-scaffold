package com.travis.monolith.app.user.internal.service.impl;

import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.infrastructure.framework.websocket.core.session.WebSocketSessionManager;
import com.travis.monolith.app.user.api.request.AppUserPageReq;
import com.travis.monolith.app.user.api.response.AppUserDashboardResp;
import com.travis.monolith.app.user.api.response.AppUserOptionResp;
import com.travis.monolith.app.user.internal.entity.AppUser;
import com.travis.monolith.app.user.internal.mapper.AppUserMapper;
import com.travis.monolith.app.user.internal.service.AppUserService;
import com.travis.monolith.system.common.api.enums.Status;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/** App 用户服务实现，负责用户分页查询及用户选项数据组装。 */
@Service
@RequiredArgsConstructor
public class AppUserServiceImpl extends ServiceImplX<AppUserMapper, AppUser>
        implements AppUserService {

    private final ObjectProvider<WebSocketSessionManager> webSocketSessionManagerProvider;

    /** 分页查询App 用户。 */
    @Override
    public PageResp<AppUserOptionResp> page(AppUserPageReq req) {
        var wrapper =
                baseOptionWrapper()
                        .likeIfPresent(AppUser::getNickname, req.getNickname())
                        .likeIfPresent(AppUser::getMobile, req.getMobile())
                        .orderByDesc(AppUser::getCreateTime);
        return PageConverter.toResp(
                page(req.getPageNum(), req.getPageSize(), wrapper).convert(this::toOption));
    }

    /** 根据 ID 集合查询App 用户选项。 */
    @Override
    public List<AppUserOptionResp> listOptionsByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return list(baseOptionWrapper().in(AppUser::getId, ids)).stream()
                .map(this::toOption)
                .toList();
    }

    /** 获取首页客户端用户概览。 */
    @Override
    public AppUserDashboardResp dashboard() {
        var summary = baseMapper.selectCountSummary(LocalDate.now().atStartOfDay());
        var sessionManager = webSocketSessionManagerProvider.getIfAvailable();
        long onlineUsers =
                sessionManager == null
                        ? 0L
                        : sessionManager.countConnectedPrincipals(LoginType.APP);
        return new AppUserDashboardResp(summary.total(), summary.newToday(), onlineUsers);
    }

    /** 构造仅包含启用用户的基础查询条件。 */
    private LambdaQueryWrapperX<AppUser> baseOptionWrapper() {
        return new LambdaQueryWrapperX<AppUser>().eq(AppUser::getStatus, Status.ENABLED.getValue());
    }

    /** 将 App 用户实体转换为选项响应。 */
    private AppUserOptionResp toOption(AppUser user) {
        return new AppUserOptionResp(
                user.getId(), user.getUsername(), user.getNickname(), user.getMobile());
    }
}
