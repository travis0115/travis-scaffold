package com.travis.monolith.system.message.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SysMessageSourceType {
    MANUAL("MANUAL");

    private final String value;
}
