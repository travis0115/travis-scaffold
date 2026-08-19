package com.travis.infrastructure.framework.websocket.core.session;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.travis.infrastructure.common.monitor.error.ErrorReporter;
import com.travis.infrastructure.framework.websocket.config.properties.WebSocketProperties;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

class LocalWebSocketSessionManagerTest {

    @Test
    void shouldNotScheduleDisconnectAfterShutdown() throws Exception {
        var manager =
                new LocalWebSocketSessionManager(
                        new WebSocketProperties(), null, null, null, mock(ErrorReporter.class));
        var session = mock(WebSocketSession.class);
        var attributes = new HashMap<String, Object>();
        attributes.put(WebSocketPrincipal.ATTR_PRINCIPAL, "admin:1");
        when(session.getAttributes()).thenReturn(attributes);
        when(session.getHandshakeHeaders()).thenReturn(HttpHeaders.EMPTY);
        when(session.getId()).thenReturn("session-1");

        manager.afterConnectionEstablished(session);
        manager.stopHeartbeat();

        assertThatCode(() -> manager.afterConnectionClosed(session, CloseStatus.NORMAL))
                .doesNotThrowAnyException();
    }
}
