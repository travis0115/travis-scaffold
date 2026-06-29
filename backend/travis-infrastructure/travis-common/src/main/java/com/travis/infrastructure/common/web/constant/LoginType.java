package com.travis.infrastructure.common.web.constant;

import cn.hutool.core.util.StrUtil;
import java.util.Locale;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/** 登录用户类型常量 */
@Slf4j
public final class LoginType {

    /** 管理后台用户 */
    public static final String ADMIN = "admin";

    /** 用户 */
    public static final String USER = "user";

    private static final Set<String> SUPPORTED_LOGIN_TYPES = Set.of(ADMIN, USER);

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
