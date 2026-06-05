package com.travis.infrastructure.common.web.enums;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 登录用户类型枚举
 */
@AllArgsConstructor
@Slf4j
@Getter
public enum LoginType {

    ADMIN("admin", "管理后台用户"),

    USER("user", "用户"),

    UNKNOWN("unknown", "未知"),

    ;

    /**
     * 登录用户类型
     */
    private final String code;

    /**
     * 类型说明
     */
    private final String description;

    private static final Map<String, LoginType> LOGIN_TYPE_MAP;

    static {
        LOGIN_TYPE_MAP = new HashMap<>();
        for (LoginType type : LoginType.values()) {
            LOGIN_TYPE_MAP.put(type.getCode().toLowerCase(Locale.ROOT), type);
        }
    }

    public static LoginType from(String raw) {
        if (StrUtil.isBlank(raw)) {
            return UNKNOWN;
        }
        String normalizedRaw = raw.trim().toLowerCase(Locale.ROOT);
        var loginType = LOGIN_TYPE_MAP.getOrDefault(normalizedRaw, UNKNOWN);
        if (loginType == UNKNOWN) {
            log.warn("无法识别的 loginType：{}", raw);
        }
        return loginType;
    }

}