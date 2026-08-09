package com.travis.infrastructure.framework.websocket.core.sender;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.travis.infrastructure.framework.websocket.core.message.WebSocketMessage;
import com.travis.infrastructure.framework.websocket.core.session.WebSocketSessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class WebSocketMessageSenderTest {

    private final WebSocketSessionManager sessionManager = mock(WebSocketSessionManager.class);
    private final WebSocketMessageSender sender = new WebSocketMessageSender(sessionManager);

    @AfterEach
    void clearTransactionSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    void shouldSendImmediatelyWithoutTransaction() {
        var principalMessage = WebSocketMessage.toPrincipal("system", "admin:1", "principal");
        var broadcastMessage = WebSocketMessage.toAll("system", "broadcast");
        var namespaceMessage =
                WebSocketMessage.toNamespace(
                        "system", "admin", () -> "changed", java.util.Map.of());

        sender.sendToPrincipal("admin:1", principalMessage);
        sender.sendToAll(broadcastMessage);
        sender.sendToNamespace("admin", namespaceMessage);

        verify(sessionManager).sendToPrincipal("admin:1", principalMessage);
        verify(sessionManager).sendToAll(broadcastMessage);
        verify(sessionManager).sendToNamespace("admin", namespaceMessage);
    }

    @Test
    void shouldSendOnlyAfterTransactionCommit() {
        beginTransactionSynchronization();
        var message = WebSocketMessage.toPrincipal("system", "admin:1", "content");

        sender.sendToPrincipal("admin:1", message);

        verify(sessionManager, never()).sendToPrincipal("admin:1", message);
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit);
        verify(sessionManager).sendToPrincipal("admin:1", message);
    }

    @Test
    void shouldNotSendAfterTransactionRollback() {
        beginTransactionSynchronization();
        var message = WebSocketMessage.toAll("system", "content");

        sender.sendToAll(message);

        TransactionSynchronizationManager.getSynchronizations()
                .forEach(
                        synchronization ->
                                synchronization.afterCompletion(
                                        TransactionSynchronization.STATUS_ROLLED_BACK));
        verify(sessionManager, never()).sendToAll(message);
    }

    private void beginTransactionSynchronization() {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        TransactionSynchronizationManager.initSynchronization();
    }
}
