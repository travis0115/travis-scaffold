package com.travis.monolith.system.message.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 消息类型枚举。 */
@Getter
@AllArgsConstructor
public enum SysMessageType {
    /** 系统通知。 */
    SYSTEM(1),

    /** 系统公告。 */
    NOTICE(2),

    /** 版本更新。 */
    VERSION(3);

    /** 消息类型值。 */
    private final Integer value;
}
