package com.travis.monolith.system.common.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 是否允许修改 */
@Getter
@AllArgsConstructor
public enum Modifiable {
    /** 禁止修改 */
    NO(0, "禁止修改"),

    /** 允许修改 */
    YES(1, "允许修改");

    /** 是否可修改的状态值。 */
    private final Integer value;

    /** 状态展示名称。 */
    private final String label;
}
