package com.travis.infrastructure.framework.websocket.core.session;

import java.util.Map;

/** WebSocket 连接主体工具。 */
public final class WebSocketPrincipal {

    /** WebSocket Session 中保存连接主体的属性名。 */
    public static final String ATTR_PRINCIPAL = "principal";

    private WebSocketPrincipal() {}

    public static String get(Map<String, Object> attributes) {
        Object principal = attributes.get(ATTR_PRINCIPAL);
        return principal == null ? null : principal.toString();
    }
}
