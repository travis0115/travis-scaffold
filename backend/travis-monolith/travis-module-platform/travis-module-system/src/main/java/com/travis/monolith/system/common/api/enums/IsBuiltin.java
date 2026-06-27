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

    private final Integer value;

    private final String label;
}
