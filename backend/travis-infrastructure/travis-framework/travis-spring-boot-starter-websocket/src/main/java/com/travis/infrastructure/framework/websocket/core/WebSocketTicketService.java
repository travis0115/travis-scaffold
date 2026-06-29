package com.travis.infrastructure.framework.websocket.core;

import com.travis.infrastructure.framework.redis.core.key.RedisKeyPrefixResolver;
import com.travis.infrastructure.framework.websocket.config.properties.WebSocketProperties;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import org.springframework.data.redis.core.RedisTemplate;

/** WebSocket 短期握手 ticket 服务。 */
public class WebSocketTicketService {

    private static final int TICKET_BYTES = 32;

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisKeyPrefixResolver redisKeyPrefixResolver;
    private final WebSocketProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public WebSocketTicketService(
            RedisTemplate<String, Object> redisTemplate,
            RedisKeyPrefixResolver redisKeyPrefixResolver,
            WebSocketProperties properties) {
        this.redisTemplate = redisTemplate;
        this.redisKeyPrefixResolver = redisKeyPrefixResolver;
        this.properties = properties;
    }

    public String create(String loginType, String userId, String token) {
        var ticket = generateTicket();
        var value = new WebSocketTicket(loginType, userId, token, System.currentTimeMillis());
        redisTemplate
                .opsForValue()
                .set(
                        buildKey(ticket),
                        value,
                        Duration.ofMillis(properties.getTicketTimeout()));
        return ticket;
    }

    public WebSocketTicket consume(String ticket) {
        if (ticket == null || ticket.isBlank()) {
            return null;
        }
        var value = redisTemplate.opsForValue().getAndDelete(buildKey(ticket.trim()));
        return value instanceof WebSocketTicket webSocketTicket ? webSocketTicket : null;
    }

    private String generateTicket() {
        var bytes = new byte[TICKET_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String buildKey(String ticket) {
        return redisKeyPrefixResolver.apply(properties.getRedis().getTicketKeyPrefix() + ":" + ticket);
    }
}
