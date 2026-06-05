package com.travis.infrastructure.common.web.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/** 平台类型枚举 */
@AllArgsConstructor
@Slf4j
@Getter
public enum PlatformType {
    /** iOS */
    IOS("ios", "iOS"),

    /** iPadOS */
    IPADOS("ipados", "iPadOS"),

    /** 安卓 Android */
    ANDROID("android", "Android"),

    /** 鸿蒙 HarmonyOS */
    HARMONYOS("harmonyos", "HarmonyOS"),

    /** MacOS */
    MACOS("macos", "macOS"),

    /** Windows */
    WINDOWS("windows", "Windows"),

    /** 兜底 */
    UNKNOWN("unknown", "未知"),
    ;

    /** 平台类型 */
    private final String code;

    /** 平台类型显示名称 未做国际化处理，若需国际化，请自行扩展（通过code） */
    private final String displayName;
}
