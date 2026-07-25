package com.travis.monolith.system.message.api.enums;

import com.travis.infrastructure.framework.websocket.core.message.WebSocketEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 系统消息 WebSocket 事件枚举。 */
@Getter
@AllArgsConstructor
public enum SysMessageWebSocketEvent implements WebSocketEvent {
    /** 消息已删除。 */
    DELETED("SYSTEM_MESSAGE_DELETED"),

    /** 收件箱内容发生变化。 */
    INBOX_CHANGED("SYSTEM_MESSAGE_INBOX_CHANGED"),

    /** 消息已发布。 */
    PUBLISHED("SYSTEM_MESSAGE_PUBLISHED"),

    /** 消息已重新发布。 */
    REPUBLISHED("SYSTEM_MESSAGE_REPUBLISHED"),

    /** 消息已撤回。 */
    REVOKED("SYSTEM_MESSAGE_REVOKED");

    /** WebSocket 事件名称。 */
    private final String event;
}
