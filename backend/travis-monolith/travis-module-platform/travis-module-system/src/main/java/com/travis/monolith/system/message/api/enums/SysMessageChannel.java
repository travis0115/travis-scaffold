package com.travis.monolith.system.message.api.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SysMessageChannel {
    IN_APP("IN_APP"),
    SMS("SMS"),
    WECHAT_MP("WECHAT_MP"),
    WECHAT_OA("WECHAT_OA");

    private static final Set<String> VALUES =
            Arrays.stream(values()).map(SysMessageChannel::getValue).collect(Collectors.toSet());

    private final String value;

    public static boolean contains(String value) {
        return VALUES.contains(value);
    }

    public static boolean isExternal(String value) {
        return SMS.value.equals(value)
                || WECHAT_MP.value.equals(value)
                || WECHAT_OA.value.equals(value);
    }
}
