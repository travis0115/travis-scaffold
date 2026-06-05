package com.travis.infrastructure.common.validation.constant;

/**
 * @author travis
 */
public final class RegexConstants {
    private RegexConstants() {}

    /** 用户名 英文字母开头的6-16位账号，支持字母、数字和下划线 */
    public static final String USERNAME = "^[a-zA-Z][a-zA-Z0-9_]{5,15}$";
}
