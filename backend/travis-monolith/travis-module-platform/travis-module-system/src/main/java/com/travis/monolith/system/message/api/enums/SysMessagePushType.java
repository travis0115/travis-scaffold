package com.travis.monolith.system.message.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 消息推送方式枚举。 */
@Getter
@AllArgsConstructor
public enum SysMessagePushType {
    /** 手动推送。 */
    MANUAL(0),

    /** 定时推送。 */
    SCHEDULED(1),

    /** 自动推送。 */
    AUTO(2);

    /** 推送方式值。 */
    private final Integer value;
}
