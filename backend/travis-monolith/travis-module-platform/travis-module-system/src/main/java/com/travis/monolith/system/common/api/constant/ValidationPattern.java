package com.travis.monolith.system.common.api.constant;

/**
 * 正则表达式
 *
 * @author travis
 */
public final class ValidationPattern {
    private ValidationPattern() {}

    /** 以字母开头，只能包含字母、数字和下划线 */
    public static final String CODE = "^[a-zA-Z][a-zA-Z0-9_]+$";
}
