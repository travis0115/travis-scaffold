package com.travis.monolith.system.common.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 是否系统内置 */
@Getter
@AllArgsConstructor
public enum IsPinned {
    /** 未置顶 */
    NO(0, "未置顶"),

    /** 置顶 */
    YES(1, "置顶");

    private final Integer value;

    private final String label;
}
