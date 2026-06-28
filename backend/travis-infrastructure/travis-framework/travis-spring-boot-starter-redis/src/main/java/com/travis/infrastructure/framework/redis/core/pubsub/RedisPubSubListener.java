package com.travis.infrastructure.framework.redis.core.pubsub;

/** Redis Pub/Sub 消息监听器。 */
@FunctionalInterface
public interface RedisPubSubListener {

    /**
     * 处理订阅消息。
     *
     * @param channel 频道
     * @param payload 消息内容
     */
    void onMessage(String channel, String payload);
}
