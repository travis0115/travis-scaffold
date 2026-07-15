package com.travis.monolith.system.common.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 是否系统内置 */
@Getter
@AllArgsConstructor
public enum IsBuiltin {
    /** 用户创建 */
    NO(0, "用户创建"),

    /** 系统内置 */
    YES(1, "系统内置");

    /** 是否内置的状态值。 */
    private final Integer value;

    /** 状态展示名称。 */
    private final String label;
}
