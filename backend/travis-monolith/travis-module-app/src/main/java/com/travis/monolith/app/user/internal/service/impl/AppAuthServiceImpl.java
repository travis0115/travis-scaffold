package com.travis.monolith.app.user.internal.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.framework.satoken.core.LoginSubjectSessionKey;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.infrastructure.framework.satoken.core.websocket.SaTokenWebSocketAuthService;
import com.travis.infrastructure.framework.satoken.core.websocket.SaTokenWebSocketPrincipal;
import com.travis.infrastructure.framework.satoken.core.websocket.ticket.SaTokenWebSocketTicketStore;
import com.travis.infrastructure.framework.websocket.core.session.WebSocketSessionManager;
import com.travis.monolith.app.user.api.request.AppUserLoginReq;
import com.travis.monolith.app.user.api.response.AppUserInfoResp;
import com.travis.monolith.app.user.api.response.AppWebSocketTicketResp;
import com.travis.monolith.app.user.internal.converter.AppUserConverter;
import com.travis.monolith.app.user.internal.entity.AppUser;
import com.travis.monolith.app.user.internal.service.AppAuthService;
import com.travis.monolith.app.user.internal.service.AppUserService;
import com.travis.monolith.system.common.api.enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** App 端认证服务实现，负责用户登录、退出、WebSocket 凭证签发及当前用户信息查询。 */
@Service
@RequiredArgsConstructor
public class AppAuthServiceImpl implements AppAuthService {

    private final AppUserService userService;
    private final AppUserConverter converter;
    private final SaTokenWebSocketTicketStore ticketStore;
    private final WebSocketSessionManager webSocketSessionManager;

    /** 执行App 端认证登录。 */
    @Override
    public void login(AppUserLoginReq req) {
        var user =
                userService
                        .lambdaQuery()
                        .eq(AppUser::getUsername, req.getUsername())
                        .select(
                                AppUser::getId,
                                AppUser::getUsername,
                                AppUser::getPassword,
                                AppUser::getStatus)
                        .one();
        if (user == null || !BCrypt.checkpw(req.getPassword(), user.getPassword())) {
            throw new BizException(CommonErrorCode.AUTH_LOGIN_BAD_CREDENTIALS);
        }
        if (Status.DISABLED.getValue().equals(user.getStatus())) {
            throw new BizException(CommonErrorCode.AUTH_LOGIN_USER_DISABLED);
        }

        var stpLogic = StpKit.of(LoginType.APP);
        stpLogic.login(user.getId());
        stpLogic.getSession().set(LoginSubjectSessionKey.USERNAME, user.getUsername());
    }

    /** 执行App 端认证退出。 */
    @Override
    public void logout() {
        var stpLogic = StpKit.of(LoginType.APP);
        var userId = stpLogic.isLogin() ? stpLogic.getLoginIdAsLong() : null;
        var token = stpLogic.getTokenValue();
        stpLogic.logout();
        if (userId != null && token != null) {
            webSocketSessionManager.close(
                    SaTokenWebSocketPrincipal.build(LoginType.APP, userId),
                    SaTokenWebSocketAuthService.ATTR_TOKEN,
                    token);
        }
    }

    /** 为当前用户签发 WebSocket 一次性连接凭证。 */
    @Override
    public AppWebSocketTicketResp createWebSocketTicket() {
        var stpLogic = StpKit.of(LoginType.APP);
        var ticket =
                ticketStore.create(
                        LoginType.APP, stpLogic.getLoginIdAsLong(), stpLogic.getTokenValue());
        return new AppWebSocketTicketResp(ticket, ticketStore.getTimeoutSeconds());
    }

    /** 查询当前登录用户信息。 */
    @Override
    public AppUserInfoResp getUserInfo() {
        return converter.toInfoResp(
                userService.getById(StpKit.of(LoginType.APP).getLoginIdAsLong()));
    }
}
