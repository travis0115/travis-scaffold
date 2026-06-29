package com.travis.monolith.system.user.internal.service;

import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.framework.satoken.core.SaTokenWebSocketTicketStore;
import com.travis.monolith.system.user.api.response.SysWebSocketTicketResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 系统模块 WebSocket 握手 ticket 签发服务。 */
@Service
@RequiredArgsConstructor
public class SysWebSocketTicketService {

    private final SaTokenWebSocketTicketStore ticketStore;

    public SysWebSocketTicketResp createAdminTicket(Long userId, String token) {
        var ticket = ticketStore.create(LoginType.ADMIN, userId, token);
        return new SysWebSocketTicketResp(ticket, ticketStore.getTimeoutSeconds());
    }
}
