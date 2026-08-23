package com.travis.monolith.system.user.internal.service.impl;

import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.framework.satoken.core.websocket.ticket.SaTokenWebSocketTicketStore;
import com.travis.monolith.system.user.api.response.SysWebSocketTicketResp;
import com.travis.monolith.system.user.internal.service.SysWebSocketTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 系统模块 WebSocket 握手 ticket 签发服务。 */
@Service
@RequiredArgsConstructor
public class SysWebSocketTicketServiceImpl implements SysWebSocketTicketService {

    private final SaTokenWebSocketTicketStore ticketStore;

    /** 为指定后台用户和登录令牌创建一次性 WebSocket 握手凭证。 */
    @Override
    public SysWebSocketTicketResp createAdminTicket(Long userId, String token) {
        var ticket = ticketStore.create(LoginType.ADMIN, userId, token);
        return new SysWebSocketTicketResp(ticket, ticketStore.getTimeoutSeconds());
    }
}
