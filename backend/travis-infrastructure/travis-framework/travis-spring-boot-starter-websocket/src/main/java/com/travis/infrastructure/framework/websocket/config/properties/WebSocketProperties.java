package com.travis.infrastructure.framework.websocket.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * WebSocket 配置属性
 *
 * <p>使用示例（application.yml）：
 *
 * <pre>{@code
 * travis:
 *   web:
 *     websocket:
 *     enabled: true
 *     path: /ws
 *     heartbeat-interval: 30000
 *     session-timeout: 300000
 *     credential-key: ticket
 *     redis:
 *       enabled: true
 *       channel: "websocket:channel:broadcast"
 *       session-key-prefix: "websocket:session"
 *       retry-interval: 30000
 * }</pre>
 *
 * @author travis
 */
@Data
@ConfigurationProperties(prefix = "travis.web.websocket")
public class WebSocketProperties {

    /** 是否启用 WebSocket（默认 true） */
    private boolean enabled = true;

    /** 心跳间隔，单位毫秒（默认 30s，<=0 表示不启用心跳） */
    private long heartbeatInterval = 30000;

    /** Session 超时时间，单位毫秒（默认 5min） */
    private long sessionTimeout = 300000;

    /** 下线确认宽限期，单位毫秒（默认 15s），用于避免刷新页面时短暂离线 */
    private long offlineGracePeriod = 15000;

    /** 握手凭证参数名 */
    private String credentialKey = "ticket";

    /** Redis 相关配置 */
    private Redis redis = new Redis();

    @Data
    public static class Redis {

        /** 是否启用 Redis Pub/Sub 集群广播 */
        private boolean enabled = true;

        /** Redis Pub/Sub 频道名称 */
        private String channel = "websocket:channel:broadcast";

        /** Session 映射 Redis key 前缀 */
        private String sessionKeyPrefix = "websocket:session";

        /** Redis 失败后的重试间隔，单位毫秒 */
        private long retryInterval = 30000;
    }
}
