package com.travis.monolith.system.menu.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 菜单类型枚举 */
@Getter
@AllArgsConstructor
public enum MenuType {
    /** 目录 */
    DIRECTORY(0, "目录"),

    /** 菜单 */
    MENU(1, "菜单"),

    /** 按钮 */
    BUTTON(2, "按钮");

    private final Integer value;

    private final String label;
}
