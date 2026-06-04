package com.travis.infrastructure.framework.web.core.filter;

import lombok.AllArgsConstructor;

/**
 *
 * @author Travis
 */
@AllArgsConstructor
public enum EventType implements com.travis.infrastructure.framework.logging.core.enums.EventType {
    TEST("test_event", "测试事件"),
    ;

    private final String code;

    private final String description;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
