package com.travis.monolith.system.message.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 消息推送状态枚举。 */
@Getter
@AllArgsConstructor
public enum SysMessageStatus {
    /** 待推送。 */
    PENDING(0),

    /** 定时推送。 */
    SCHEDULED(1),

    /** 已推送。 */
    SENT(2),

    /** 已撤回。 */
    REVOKED(3);

    private final Integer value;
}
