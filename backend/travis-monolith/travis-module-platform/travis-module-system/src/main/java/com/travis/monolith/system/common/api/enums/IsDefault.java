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

    /** 是否默认的状态值。 */
    private final Integer value;

    /** 状态展示名称。 */
    private final String label;
}
