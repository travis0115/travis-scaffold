package com.travis.monolith.system.common.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 是否默认 */
@Getter
@AllArgsConstructor
public enum IsDefault {
    /** 非默认 */
    NO(0, "非默认"),

    /** 默认 */
    YES(1, "默认");

    private final Integer value;

    private final String label;
}
