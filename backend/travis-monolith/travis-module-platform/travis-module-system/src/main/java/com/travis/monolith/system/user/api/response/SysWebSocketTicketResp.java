package com.travis.monolith.system.user.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** WebSocket 握手 ticket 响应。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysWebSocketTicketResp {

    private String ticket;

    private long expiresIn;
}
