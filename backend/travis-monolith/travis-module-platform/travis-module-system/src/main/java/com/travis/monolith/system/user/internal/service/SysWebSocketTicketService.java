package com.travis.monolith.system.user.internal.service;

import com.travis.monolith.system.user.api.response.SysWebSocketTicketResp;

/** 系统模块 WebSocket 握手 ticket 签发服务。 */
public interface SysWebSocketTicketService {

    /** 为指定后台用户和登录令牌创建一次性 WebSocket 握手凭证。 */
    SysWebSocketTicketResp createAdminTicket(Long userId, String token);
}
