package com.travis.monolith.system.message.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 消息接收范围枚举。 */
@Getter
@AllArgsConstructor
public enum SysMessageReceiverScope {
    /** 全部用户。 */
    ALL(0),

    /** 指定用户。 */
    USER(1),

    /** 指定角色。 */
    ROLE(2),

    /** 指定部门。 */
    DEPT(3);

    private final Integer value;

    public static boolean contains(Integer value) {
        if (value == null) {
            return false;
        }
        for (SysMessageReceiverScope item : values()) {
            if (item.value.equals(value)) {
                return true;
            }
        }
        return false;
    }
}
