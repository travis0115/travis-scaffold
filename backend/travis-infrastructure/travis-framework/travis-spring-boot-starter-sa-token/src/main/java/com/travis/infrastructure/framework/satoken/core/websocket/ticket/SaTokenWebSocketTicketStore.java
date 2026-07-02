package com.travis.infrastructure.framework.satoken.core.websocket.ticket;

/** Sa-Token WebSocket 短期握手 ticket 存储。 */
public interface SaTokenWebSocketTicketStore {

    String create(String loginType, Object loginId, String token);

    SaTokenWebSocketTicket consume(String loginType, String ticket);

    long getTimeoutSeconds();
}
