package com.travis.infrastructure.framework.websocket.core.session;

import com.travis.infrastructure.framework.websocket.core.message.WebSocketMessage;
import java.util.Set;

/**
 * WebSocket Session 管理接口，屏蔽底层 Session 存储和集群同步细节。
 *
 * <p>starter 不解释连接主体含义，业务或认证适配层自行约定 principal 格式。
 *
 * <p>业务层通过 {@link com.travis.infrastructure.framework.websocket.core.sender.WebSocketMessageSender}
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
     * 广播消息给所有已连接主体
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
     * 关闭指定连接主体下匹配属性的连接
     *
     * @param principal 连接主体
     * @param attributeName Session 属性名
     * @param attributeValue Session 属性值
     */
    void close(String principal, String attributeName, Object attributeValue);

    /**
     * 立即关闭指定连接主体的所有连接，并跳过延迟断连确认
     *
     * @param principal 连接主体
     */
    void closeImmediately(String principal);

    /**
     * 判断连接主体是否已连接（集群范围）
     *
     * @param principal 连接主体
     * @return 是否已连接
     */
    boolean isConnected(String principal);

    /**
     * 获取指定命名空间下所有已连接主体标识（集群范围）
     *
     * @param namespace 连接命名空间
     * @return 已连接主体标识集合
     */
    Set<String> getConnectedPrincipals(String namespace);

    /**
     * 获取指定命名空间下已连接主体数量（集群范围）
     *
     * @param namespace 连接命名空间
     * @return 已连接主体数量
     */
    long countConnectedPrincipals(String namespace);
}
