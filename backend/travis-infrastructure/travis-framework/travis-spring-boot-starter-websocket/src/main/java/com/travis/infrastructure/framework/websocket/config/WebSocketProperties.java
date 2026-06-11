package com.travis.infrastructure.framework.websocket.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * WebSocket 配置属性
 *
 * <p>使用示例（application.yml）：
 *
 * <pre>{@code
 * travis:
 *   websocket:
 *     enabled: true
 *     path: /ws
 *     allowed-origins: "*"
 *     heartbeat-interval: 30000
 *     session-timeout: 300000
 *     redis:
 *       channel: "travis:websocket:broadcast"
 *       session-key-prefix: "travis:ws:session:"
 * }</pre>
 *
 * @author travis
 */
@Data
@ConfigurationProperties(prefix = "travis.websocket")
public class WebSocketProperties {

    /** 是否启用 WebSocket（默认 true） */
    private boolean enabled = true;

    /** WebSocket 端点路径（默认 /ws） */
    private String path = "/ws";

    /** 允许的跨域来源（默认 *，即允许所有） */
    private String allowedOrigins = "*";

    /** 心跳间隔，单位毫秒（默认 30s，<=0 表示不启用心跳） */
    private long heartbeatInterval = 30000;

    /** Session 超时时间，单位毫秒（默认 5min） */
    private long sessionTimeout = 300000;

    /** Redis 相关配置 */
    private Redis redis = new Redis();

    @Data
    public static class Redis {

        /** Redis Pub/Sub 频道名称 */
        private String channel = "travis:websocket:broadcast";

        /** Session 映射 Redis key 前缀 */
        private String sessionKeyPrefix = "travis:ws:session:";
    }
}
