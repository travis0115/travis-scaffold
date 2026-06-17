package com.travis.monolith.system.common.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 是否允许修改 */
@Getter
@AllArgsConstructor
public enum Modifiable {
    /** 禁止修改 */
    IMMUTABLE(0, "禁止修改"),

    /** 允许修改 */
    MODIFIABLE(1, "允许修改");

    private final Integer value;

    private final String label;
}
