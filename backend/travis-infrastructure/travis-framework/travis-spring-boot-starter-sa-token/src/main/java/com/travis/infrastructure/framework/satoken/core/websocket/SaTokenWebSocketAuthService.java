package com.travis.infrastructure.framework.satoken.core.websocket;

import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.infrastructure.framework.satoken.core.websocket.ticket.SaTokenWebSocketTicketStore;
import com.travis.infrastructure.framework.websocket.core.auth.WebSocketAuthContext;
import com.travis.infrastructure.framework.websocket.core.auth.WebSocketAuthRequest;
import com.travis.infrastructure.framework.websocket.core.auth.WebSocketAuthService;
import java.util.List;
import java.util.Map;

/** 基于 Sa-Token 的 WebSocket 认证适配实现。 */
public class SaTokenWebSocketAuthService implements WebSocketAuthService {

    public static final String ATTR_LOGIN_TYPE = "saTokenLoginType";
    public static final String ATTR_LOGIN_ID = "saTokenLoginId";
    public static final String ATTR_TOKEN = "saTokenToken";

    private final SaTokenWebSocketTicketStore ticketStore;
    private final List<SaTokenWebSocketSubjectValidator> subjectValidators;

    public SaTokenWebSocketAuthService(
            SaTokenWebSocketTicketStore ticketStore,
            List<SaTokenWebSocketSubjectValidator> subjectValidators) {
        this.ticketStore = ticketStore;
        this.subjectValidators = subjectValidators == null ? List.of() : subjectValidators;
    }

    @Override
    public WebSocketAuthContext authenticate(WebSocketAuthRequest request) {
        var loginType = getString(request.attributes(), ATTR_LOGIN_TYPE);
        if (loginType == null) {
            return null;
        }
        var webSocketTicket = ticketStore.consume(loginType, request.credential());
        if (webSocketTicket == null) {
            return null;
        }
        var attributes = webSocketTicket.attributes();
        var loginId = getString(attributes, ATTR_LOGIN_ID);
        var token = getString(attributes, ATTR_TOKEN);
        if (loginId == null || token == null) {
            return null;
        }
        Object currentLoginId = StpKit.of(loginType).getLoginIdByToken(token);
        if (currentLoginId == null || !loginId.equals(currentLoginId.toString())) {
            return null;
        }
        if (!isSubjectValid(loginType, loginId)) {
            return null;
        }
        return new WebSocketAuthContext(webSocketTicket.principal(), attributes);
    }

    @Override
    public boolean isConnectionValid(WebSocketAuthContext context) {
        var attributes = context.attributes();
        var loginType = getString(attributes, ATTR_LOGIN_TYPE);
        var loginId = getString(attributes, ATTR_LOGIN_ID);
        var token = getString(attributes, ATTR_TOKEN);
        if (loginType == null || loginId == null || token == null) {
            return false;
        }
        Object currentLoginId = StpKit.of(loginType).getLoginIdByToken(token);
        return currentLoginId != null
                && loginId.equals(currentLoginId.toString())
                && isSubjectValid(loginType, loginId);
    }

    private String getString(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        if (value == null) {
            return null;
        }
        var stringValue = value.toString();
        return stringValue.isBlank() ? null : stringValue;
    }

    private boolean isSubjectValid(String loginType, String loginId) {
        return subjectValidators.stream()
                .filter(validator -> validator.supports(loginType))
                .allMatch(validator -> validator.isValid(loginType, loginId));
    }
}
