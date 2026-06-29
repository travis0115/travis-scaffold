package com.travis.infrastructure.framework.satoken.core;

/** Sa-Token WebSocket 连接主体规则。 */
public final class SaTokenWebSocketPrincipal {

    private static final String SEPARATOR = ":";

    private SaTokenWebSocketPrincipal() {}

    public static String build(String loginType, Object loginId) {
        return loginType + SEPARATOR + loginId;
    }

    public static Subject parse(String principal) {
        if (principal == null || principal.isBlank()) {
            return null;
        }
        var index = principal.indexOf(SEPARATOR);
        if (index <= 0 || index == principal.length() - 1) {
            return null;
        }
        return new Subject(principal.substring(0, index), principal.substring(index + 1));
    }

    public record Subject(String loginType, String loginId) {}
}
