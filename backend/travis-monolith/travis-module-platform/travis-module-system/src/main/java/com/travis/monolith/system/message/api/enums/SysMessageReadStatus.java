package com.travis.monolith.system.message.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 消息阅读状态枚举。 */
@Getter
@AllArgsConstructor
public enum SysMessageReadStatus {
    /** 未读。 */
    UNREAD(0),

    /** 已读。 */
    READ(1),

    /** 已删除。 */
    DELETED(2);

    /** 阅读状态值。 */
    private final Integer value;
}
