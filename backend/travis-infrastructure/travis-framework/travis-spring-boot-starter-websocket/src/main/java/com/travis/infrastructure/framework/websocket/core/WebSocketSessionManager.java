package com.travis.infrastructure.framework.websocket.core;

import com.travis.infrastructure.framework.websocket.message.WebSocketMessage;
import java.util.Set;

/**
 * WebSocket Session 管理接口，屏蔽底层 Session 存储和集群同步细节。
 *
 * <p>starter 不解释连接主体含义，业务或认证适配层自行约定 principal 格式。
 *
 * <p>业务层通过 {@link com.travis.infrastructure.framework.websocket.core.WebSocketMessageSender}
 * 间接使用，一般不需要直接注入此接口。
 *
 * @author travis
 */
public interface WebSocketSessionManager {

    /**
     * 发送消息给指定连接主体
     *
     * @param principal 连接主体
     * @param message 消息体
     */
    void sendToPrincipal(String principal, WebSocketMessage message);

    /**
     * 广播消息给所有在线用户
     *
     * @param message 消息体
     */
    void sendToAll(WebSocketMessage message);

    /**
     * 关闭指定连接主体的所有连接
     *
     * @param principal 连接主体
     */
    void close(String principal);

    /**
     * 判断连接主体是否在线（集群范围）
     *
     * @param principal 连接主体
     * @return 是否在线
     */
    boolean isOnline(String principal);

    /**
     * 获取所有在线连接主体标识（集群范围）
     *
     * @return 在线用户标识集合
     */
    Set<String> getOnlineUsers();
}
