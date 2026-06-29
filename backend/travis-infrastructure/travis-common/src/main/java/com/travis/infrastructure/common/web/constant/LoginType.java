package com.travis.infrastructure.common.web.constant;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;
import java.util.Set;

/** 登录用户类型常量 */
@Slf4j
public final class LoginType {

    /** 管理后台 */
    public static final String ADMIN = "admin";

    /** 客户端 */
    public static final String CLIENT = "client";

    private static final Set<String> SUPPORTED_LOGIN_TYPES = Set.of(ADMIN, CLIENT);

    private LoginType() {}

    public static String from(String raw) {
        if (StrUtil.isBlank(raw)) {
            return CommonConstant.UNKNOWN;
        }
        String normalizedRaw = raw.trim().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_LOGIN_TYPES.contains(normalizedRaw)) {
            log.warn("无法识别的 loginType：{}", raw);
            return CommonConstant.UNKNOWN;
        }
        return normalizedRaw;
    }
}
