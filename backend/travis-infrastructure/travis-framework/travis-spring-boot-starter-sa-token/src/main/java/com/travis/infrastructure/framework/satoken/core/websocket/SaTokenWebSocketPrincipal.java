package com.travis.infrastructure.framework.satoken.core.websocket;

/** Sa-Token WebSocket 连接主体规则。 */
public final class SaTokenWebSocketPrincipal {

    /** 主体标识中登录体系与登录 ID 的分隔符。 */
    private static final String SEPARATOR = ":";

    private SaTokenWebSocketPrincipal() {}

    public static String build(String loginType, Object loginId) {
        return loginType + SEPARATOR + loginId;
    }

    /** 将连接主体标识解析为登录体系和登录 ID。 */
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

    /**
     * 解析后的 Sa-Token 连接主体。
     *
     * @param loginType 登录体系
     * @param loginId 登录 ID
     */
    public record Subject(String loginType, String loginId) {}
}
