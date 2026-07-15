package com.travis.monolith.app.user.internal.service;

import com.travis.monolith.app.user.api.request.AppUserLoginReq;
import com.travis.monolith.app.user.api.response.AppUserInfoResp;
import com.travis.monolith.app.user.api.response.AppWebSocketTicketResp;

/** 客户端认证服务。 */
public interface AppAuthService {
    /** 校验账号凭据并建立客户端登录会话。 */
    void login(AppUserLoginReq req);

    /** 退出当前客户端登录会话，并关闭该令牌关联的 WebSocket 连接。 */
    void logout();

    /** 为当前用户创建一次性 WebSocket 握手凭证。 */
    AppWebSocketTicketResp createWebSocketTicket();

    /** 获取当前登录用户信息。 */
    AppUserInfoResp getUserInfo();
}
