package com.travis.infrastructure.framework.websocket.core.sender;

import com.travis.infrastructure.common.monitor.error.ErrorReporter;
import com.travis.infrastructure.common.monitor.error.ErrorSource;
import com.travis.infrastructure.common.transaction.AfterCommitExecutor;
import com.travis.infrastructure.framework.websocket.core.message.WebSocketMessage;
import com.travis.infrastructure.framework.websocket.core.session.WebSocketSessionManager;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket 消息发送工具类，供业务层直接注入使用。
 *
 * <p>发送操作在事务中调用时延迟到事务提交成功后执行；没有事务时立即执行。
 *
 * <p>使用示例：
 *
 * <pre>{@code
 * @Service
 * @RequiredArgsConstructor
 * public class NotificationService {
 *
 *     private final WebSocketMessageSender wsSender;
 *
 *     public void notifyAdmin(Long adminId, String msg) {
 *         wsSender.sendToPrincipal("admin:" + adminId,
 *                 WebSocketMessage.toPrincipal(
 *                         WebSocketSender.SYSTEM, "admin:" + adminId, msg));
 *     }
 *
 *     public void pushMarketData(MarketDataVO data) {
 *         wsSender.sendToAll(WebSocketMessage.toAll("market", data));
 *     }
 * }
 * }</pre>
 *
 * @author travis
 */
@RequiredArgsConstructor
@Slf4j
public class WebSocketMessageSender {

    private final WebSocketSessionManager sessionManager;
    private final ErrorReporter errorReporter;

    /**
     * 发送消息给指定连接主体
     *
     * @param principal 连接主体
     * @param message 消息体
     */
    public void sendToPrincipal(String principal, WebSocketMessage message) {
        sendAfterCommit(() -> sessionManager.sendToPrincipal(principal, message));
    }

    /**
     * 广播消息给所有已连接主体
     *
     * @param message 消息体
     */
    public void sendToAll(WebSocketMessage message) {
        sendAfterCommit(() -> sessionManager.sendToAll(message));
    }

    /** 向指定连接命名空间广播消息。 */
    public void sendToNamespace(String namespace, WebSocketMessage message) {
        sendAfterCommit(() -> sessionManager.sendToNamespace(namespace, message));
    }

    /**
     * 获取指定命名空间下所有已连接主体。
     *
     * @param namespace 连接命名空间
     * @return 已连接主体集合
     */
    public Set<String> getConnectedPrincipals(String namespace) {
        return sessionManager.getConnectedPrincipals(namespace);
    }

    /**
     * 判断连接主体是否已连接
     *
     * @param principal 连接主体
     * @return 是否已连接
     */
    public boolean isConnected(String principal) {
        return sessionManager.isConnected(principal);
    }

    private void sendAfterCommit(Runnable sender) {
        AfterCommitExecutor.execute(
                () -> {
                    try {
                        sender.run();
                    } catch (RuntimeException exception) {
                        log.error("[WebSocket] 消息发送失败", exception);
                        errorReporter.report(
                                ErrorSource.WEBSOCKET,
                                getClass().getName() + "#sendAfterCommit",
                                null,
                                exception);
                    }
                });
    }
}
