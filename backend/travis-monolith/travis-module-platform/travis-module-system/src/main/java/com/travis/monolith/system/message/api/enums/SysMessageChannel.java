package com.travis.monolith.system.message.api.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 消息推送通道枚举。 */
@Getter
@AllArgsConstructor
public enum SysMessageChannel {
    /** 站内信。 */
    IN_APP("IN_APP"),

    /** 短信。 */
    SMS("SMS"),

    /** 微信小程序。 */
    WECHAT_MP("WECHAT_MP"),

    /** 微信公众号。 */
    WECHAT_OA("WECHAT_OA");

    private static final Set<String> VALUES =
            Arrays.stream(values()).map(SysMessageChannel::getValue).collect(Collectors.toSet());

    /** 推送通道值。 */
    private final String value;

    public static boolean contains(String value) {
        return VALUES.contains(value);
    }

    public static boolean isExternal(String value) {
        return !IN_APP.value.equals(value);
    }
}
