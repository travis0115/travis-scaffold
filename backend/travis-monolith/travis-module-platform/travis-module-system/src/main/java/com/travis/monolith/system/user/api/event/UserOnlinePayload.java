package com.travis.monolith.system.user.api.event;

import lombok.Builder;

/**
 * 用户 WebSocket 在线状态变更事件载荷
 *
 * @author travis
 */
@Builder
public record UserOnlinePayload(
        /* 登录类型（如 "admin"、"user"） */
        String loginType,
        /* 用户 ID */
        String userId,
        /* 是否在线：true=上线，false=下线 */
        boolean online) {}
