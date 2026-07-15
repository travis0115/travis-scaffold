package com.travis.monolith.app.user.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 客户端 WebSocket 单次握手凭证。 */
@Data
@AllArgsConstructor
public class AppWebSocketTicketResp {
    /** 单次使用的握手凭证。 */
    private String ticket;

    /** 凭证有效期，单位秒。 */
    private long timeoutSeconds;
}
