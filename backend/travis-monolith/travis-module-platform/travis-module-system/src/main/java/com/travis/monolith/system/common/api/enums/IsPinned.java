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

    /** 是否置顶的状态值。 */
    private final Integer value;

    /** 状态展示名称。 */
    private final String label;
}
