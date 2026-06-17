package com.travis.monolith.system.common.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 状态枚举 */
@Getter
@AllArgsConstructor
public enum Status {
    /** 禁用 */
    DISABLED(0, "禁用"),

    /** 启用 */
    ENABLED(1, "启用");

    private final Integer value;

    private final String label;
}
