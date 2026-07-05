package com.travis.monolith.app.user.internal.service;

import com.travis.monolith.app.user.api.request.AppUserLoginReq;
import com.travis.monolith.app.user.api.response.AppUserInfoResp;
import com.travis.monolith.app.user.api.response.AppWebSocketTicketResp;

public interface AppAuthService {
    void login(AppUserLoginReq req);

    void logout();

    AppWebSocketTicketResp createWebSocketTicket();

    AppUserInfoResp getUserInfo();
}
