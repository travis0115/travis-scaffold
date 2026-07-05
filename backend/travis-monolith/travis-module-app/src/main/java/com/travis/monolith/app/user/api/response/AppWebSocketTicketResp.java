package com.travis.monolith.app.user.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppWebSocketTicketResp {
    private String ticket;
    private long timeoutSeconds;
}
