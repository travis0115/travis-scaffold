package com.travis.infrastructure.framework.redis.core.pubsub;

import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/** Redis Pub/Sub 发布订阅客户端。 */
@RequiredArgsConstructor
public class RedisPubSubClient {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisMessageListenerContainer listenerContainer;

    /**
     * 发布消息。
     *
     * @param channel 频道
     * @param payload 消息内容
     */
    public void publish(String channel, String payload) {
        redisTemplate.convertAndSend(channel, payload);
    }

    /**
     * 订阅频道。
     *
     * @param channel 频道
     * @param listener 消息监听器
     */
    public void subscribe(String channel, RedisPubSubListener listener) {
        listenerContainer.addMessageListener(
                (message, _) -> {
                    String payload = new String(message.getBody(), StandardCharsets.UTF_8);
                    listener.onMessage(channel, payload);
                },
                new ChannelTopic(channel));
    }
}
