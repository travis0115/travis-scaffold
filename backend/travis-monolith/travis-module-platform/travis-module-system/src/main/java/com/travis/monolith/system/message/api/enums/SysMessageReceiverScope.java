package com.travis.monolith.system.message.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SysMessageReceiverScope {
    ALL(0),
    USER(1),
    ROLE(2),
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
