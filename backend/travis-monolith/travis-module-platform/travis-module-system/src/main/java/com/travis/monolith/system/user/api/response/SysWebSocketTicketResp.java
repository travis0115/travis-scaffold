package com.travis.monolith.system.user.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** WebSocket 握手 ticket 响应。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysWebSocketTicketResp {

    /** 单次使用的 WebSocket 握手凭证。 */
    private String ticket;

    /** 凭证有效期，单位秒。 */
    private long expiresIn;
}
