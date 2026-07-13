package com.travis.monolith.system.message.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 消息来源类型枚举。 */
@Getter
@AllArgsConstructor
public enum SysMessageSourceType {
    /** 后台人工推送。 */
    MANUAL("MANUAL"),

    /** 系统公告。 */
    NOTICE("NOTICE"),

    /** 版本更新。 */
    VERSION("VERSION");

    private final String value;
}
