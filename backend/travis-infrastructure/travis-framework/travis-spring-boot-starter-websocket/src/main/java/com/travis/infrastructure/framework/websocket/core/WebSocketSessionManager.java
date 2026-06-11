package com.travis.infrastructure.framework.websocket.core;

import com.travis.infrastructure.framework.websocket.message.WebSocketMessage;
import java.util.Set;

/**
 * WebSocket Session 管理接口，屏蔽底层 Session 存储和集群同步细节。
 *
 * <p>所有用户标识均使用 {@code loginType:userId} 复合键，支持多端（admin/app）同 ID 用户共存。
 *
 * <p>业务层通过 {@link com.travis.infrastructure.framework.websocket.core.WebSocketMessageSender}
 * 间接使用，一般不需要直接注入此接口。
 *
 * @author travis
 */
public interface WebSocketSessionManager {

    /**
     * 发送消息给指定用户
     *
     * @param loginType 登录类型（如 "admin"、"user"），对应 {@link
     *     com.travis.infrastructure.common.web.enums.LoginType}
     * @param userId 目标用户 ID
     * @param message 消息体
     */
    void sendToUser(String loginType, String userId, WebSocketMessage message);

    /**
     * 广播消息给所有在线用户
     *
     * @param message 消息体
     */
    void sendToAll(WebSocketMessage message);

    /**
     * 判断用户是否在线（集群范围）
     *
     * @param loginType 登录类型
     * @param userId 用户 ID
     * @return 是否在线
     */
    boolean isOnline(String loginType, String userId);

    /**
     * 获取所有在线用户标识（集群范围）
     *
     * <p>返回值为 {@code loginType:userId} 复合键集合，如 {@code ["admin:1", "user:100"]}
     *
     * @return 在线用户标识集合
     */
    Set<String> getOnlineUsers();
}
