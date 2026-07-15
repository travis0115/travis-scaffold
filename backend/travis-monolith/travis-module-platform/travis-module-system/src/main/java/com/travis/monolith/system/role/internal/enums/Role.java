package com.travis.monolith.system.role.internal.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 菜单类型枚举 */
@Getter
@AllArgsConstructor
public enum Role {
    /** 管理员 */
    ADMIN("admin", "管理员"),
    ;

    /** 角色编码。 */
    private final String value;

    /** 角色展示名称。 */
    private final String label;
}
