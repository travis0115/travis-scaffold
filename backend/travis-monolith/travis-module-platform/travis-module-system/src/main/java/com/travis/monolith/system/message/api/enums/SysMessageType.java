package com.travis.monolith.system.message.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 消息类型枚举。 */
@Getter
@AllArgsConstructor
public enum SysMessageType {
    /** 系统通知。 */
    SYSTEM(1),

    /** 业务消息。 */
    BUSINESS(2),

    /** 系统公告。 */
    NOTICE(3),

    /** 版本更新。 */
    VERSION_UPDATE(4);

    private final Integer value;
}
