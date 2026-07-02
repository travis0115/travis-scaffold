package com.travis.infrastructure.framework.websocket.core.session;

import java.util.Map;

/** WebSocket 连接命名空间。 */
public final class WebSocketNamespace {

    public static final String ATTR_NAMESPACE = "webSocketNamespace";
    public static final String DEFAULT_NAMESPACE = "default";

    private WebSocketNamespace() {}

    public static String get(Map<String, Object> attributes) {
        if (attributes == null) {
            return DEFAULT_NAMESPACE;
        }
        Object namespace = attributes.get(ATTR_NAMESPACE);
        if (namespace == null || namespace.toString().isBlank()) {
            return DEFAULT_NAMESPACE;
        }
        return namespace.toString();
    }
}
