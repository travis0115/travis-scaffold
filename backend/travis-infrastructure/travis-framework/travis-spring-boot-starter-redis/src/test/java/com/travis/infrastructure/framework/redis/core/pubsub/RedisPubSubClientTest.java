package com.travis.infrastructure.framework.redis.core.pubsub;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.travis.infrastructure.framework.redis.core.key.RedisKeyPrefixResolver;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

class RedisPubSubClientTest {

    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    private final RedisMessageListenerContainer listenerContainer =
            mock(RedisMessageListenerContainer.class);
    private final RedisKeyPrefixResolver keyPrefixResolver = mock(RedisKeyPrefixResolver.class);
    private final RedisPubSubClient client =
            new RedisPubSubClient(redisTemplate, listenerContainer, keyPrefixResolver);

    @Test
    void shouldPublishJsonAsPlainString() {
        when(keyPrefixResolver.apply("websocket:messages")).thenReturn("travis:websocket:messages");
        String payload = "{\"type\":\"NAMESPACE\"}";

        client.publish("websocket:messages", payload);

        verify(redisTemplate).convertAndSend("travis:websocket:messages", payload);
    }

    @Test
    void shouldDeliverRawMessageBodyToSubscriber() {
        when(keyPrefixResolver.apply("websocket:messages")).thenReturn("travis:websocket:messages");
        var listener = mock(RedisPubSubListener.class);
        var messageListenerCaptor = ArgumentCaptor.forClass(MessageListener.class);

        client.subscribe("websocket:messages", listener);
        verify(listenerContainer)
                .addMessageListener(
                        messageListenerCaptor.capture(),
                        eq(new ChannelTopic("travis:websocket:messages")));

        String payload = "{\"type\":\"NAMESPACE\"}";
        messageListenerCaptor
                .getValue()
                .onMessage(
                        new DefaultMessage(
                                "travis:websocket:messages".getBytes(StandardCharsets.UTF_8),
                                payload.getBytes(StandardCharsets.UTF_8)),
                        null);

        verify(listener).onMessage("websocket:messages", payload);
    }
}
