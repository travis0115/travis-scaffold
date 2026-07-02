package com.travis.infrastructure.framework.satoken.core.websocket;

/** Sa-Token WebSocket 连接主体业务校验扩展点。 */
public interface SaTokenWebSocketSubjectValidator {

    /** 是否处理该 loginType。 */
    boolean supports(String loginType);

    /** 校验账号业务状态是否允许保持 WebSocket 连接。 */
    boolean isValid(String loginType, String loginId);
}
