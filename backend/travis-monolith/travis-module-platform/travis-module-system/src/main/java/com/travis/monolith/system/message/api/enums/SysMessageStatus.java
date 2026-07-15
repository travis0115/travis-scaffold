package com.travis.monolith.system.message.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 消息推送状态枚举。 */
@Getter
@AllArgsConstructor
public enum SysMessageStatus {
    /** 待推送。 */
    PENDING(0),

    /** 已推送。 */
    SENT(1),

    /** 已撤回。 */
    REVOKED(2);

    /** 消息状态值。 */
    private final Integer value;
}
