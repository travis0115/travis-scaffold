package com.travis.infrastructure.framework.satoken.core.websocket.ticket;

import cn.dev33.satoken.dao.SaTokenDao;
import com.travis.infrastructure.framework.satoken.core.websocket.SaTokenWebSocketAuthService;
import com.travis.infrastructure.framework.satoken.core.websocket.SaTokenWebSocketPrincipal;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import lombok.AllArgsConstructor;

/** 基于 Sa-Token Dao 的 WebSocket ticket 存储，跟随 Sa-Token 当前存储实现。 */
@AllArgsConstructor
public class DefaultSaTokenWebSocketTicketStore implements SaTokenWebSocketTicketStore {

    /** 握手凭证使用的随机字节数。 */
    private static final int TICKET_BYTES = 32;

    /** 握手凭证默认有效期，单位秒。 */
    private static final long TICKET_TIMEOUT_SECONDS = 60;

    private final SaTokenDao saTokenDao;
    private final String tokenName;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String create(String loginType, Object loginId, String token) {
        var ticket = generateTicket();
        var principal = SaTokenWebSocketPrincipal.build(loginType, loginId);
        var value =
                new SaTokenWebSocketTicket(
                        principal,
                        Map.of(
                                SaTokenWebSocketAuthService.ATTR_LOGIN_TYPE,
                                loginType,
                                SaTokenWebSocketAuthService.ATTR_LOGIN_ID,
                                loginId.toString(),
                                SaTokenWebSocketAuthService.ATTR_TOKEN,
                                token),
                        System.currentTimeMillis());
        saTokenDao.setObject(buildKey(loginType, ticket), value, TICKET_TIMEOUT_SECONDS);
        return ticket;
    }

    @Override
    public SaTokenWebSocketTicket consume(String loginType, String ticket) {
        if (ticket == null || ticket.isBlank()) {
            return null;
        }
        var key = buildKey(loginType, ticket);
        var value = saTokenDao.getObject(key, SaTokenWebSocketTicket.class);
        saTokenDao.deleteObject(key);
        return value;
    }

    public long getTimeoutSeconds() {
        return TICKET_TIMEOUT_SECONDS;
    }

    private String generateTicket() {
        var bytes = new byte[TICKET_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String buildKey(String loginType, String ticket) {
        return tokenName + ":" + loginType + ":ws-ticket:" + ticket.trim();
    }
}
