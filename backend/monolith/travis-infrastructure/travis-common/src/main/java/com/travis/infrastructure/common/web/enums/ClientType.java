package com.travis.infrastructure.common.web.enums;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 客户端类型枚举
 */
@AllArgsConstructor
@Slf4j
@Getter
public enum ClientType {
    /**
     * PC端浏览器
     */
    PC_WEB("pc_web", "PC端浏览器"),

    /**
     * 移动端浏览器
     */
    MOBILE_WEB("mobile_web", "移动端浏览器"),

    /**
     * iPhone iOS
     */
    IOS("ios", "iOS"),

    /**
     * 安卓 Android
     */
    ANDROID("android", "Android"),

    /**
     * 鸿蒙 HarmonyOS
     */
    HARMONYOS("harmonyos", "HarmonyOS"),

    /**
     * MacOS桌面端
     */
    MACOS("macos", "MacOS"),

    /**
     * Windows桌面端
     */
    WINDOWS("windows", "Windows"),

    /**
     * 微信小程序
     */
    WECHAT_MP("wechat_mp", "微信小程序"),

    /**
     * 支付宝小程序
     */
    ALIPAY_MP("alipay_mp", "支付宝小程序"),

    /**
     * 抖音小程序
     */
    DOUYIN_MP("douyin_mp", "抖音小程序"),

    /**
     * 微信公众号
     */
    WECHAT_OFFICIAL("wechat_official", "微信公众号"),

    /**
     * API调用
     */
    API("api", "API"),

    /**
     * 兜底
     */
    UNKNOWN("unknown", "未知"),

    ;

    /**
     * 客户端类型
     */

    private final String code;

    /**
     * 客户端类型显示名称
     * 未做国际化处理，若需国际化，请自行扩展（通过code）
     */
    private final String displayName;

    private static final Map<String, ClientType> CLIENT_TYPE_MAP;

    static {
        CLIENT_TYPE_MAP = new HashMap<>();
        for (ClientType type : ClientType.values()) {
            CLIENT_TYPE_MAP.put(type.getCode().toLowerCase(Locale.ROOT), type);
        }
    }

    public static ClientType from(String raw) {
        if (StrUtil.isBlank(raw)) {
            log.warn("未传入 clientType：{}", raw);
            return UNKNOWN;
        }
        String normalizedRaw = raw.trim().toLowerCase(Locale.ROOT);
        var clientType = CLIENT_TYPE_MAP.getOrDefault(normalizedRaw, UNKNOWN);
        if (clientType == UNKNOWN) {
            log.warn("无法识别的 clientType：{}", raw);
        }
        return clientType;
    }
}