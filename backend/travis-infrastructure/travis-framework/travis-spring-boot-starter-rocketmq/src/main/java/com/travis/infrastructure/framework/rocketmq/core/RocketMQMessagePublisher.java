package com.travis.infrastructure.framework.rocketmq.core;

import com.travis.infrastructure.common.event.*;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.producer.SendReceipt;

/**
 * 基于 RocketMQ 的 {@link MessagePublisher} 实现。
 *
 * <p>将 {@link Event} 的 {@code topic} 映射为 Topic，{@code type} 映射为 Tag， 并根据 {@link
 * Event#getTopicType()} 自动选择投递方式：
 *
 * <ul>
 *   <li>{@link TopicType#NORMAL} — 普通消息
 *   <li>{@link TopicType#FIFO} — 顺序消息，需通过 {@link PublishOptions#fifo(String)} 提供 messageGroup
 *   <li>{@link TopicType#DELAY} — 延迟消息，需通过 {@link PublishOptions#delay(java.time.Duration)} 提供
 *       delayTime
 * </ul>
 *
 * @author travis
 * @see MessagePublisher
 * @see Event
 */
@Slf4j
public class RocketMQMessagePublisher implements MessagePublisher {

    /** 将 Event 解析为 RocketMQ destination 格式：{@code topic:type} */
    private static String toDestination(Event event) {
        return event.getTopic() + ":" + event.getType();
    }

    // ==================== 同步发布 ====================

    @Override
    public void publish(Event event, Object payload) {
        var destination = toDestination(event);
        switch (event.getTopicType()) {
            case FIFO ->
                    throw new IllegalArgumentException(
                            "FIFO event requires messageGroup, use publish(event, payload, PublishOptions.fifo(group))");
            case DELAY ->
                    throw new IllegalArgumentException(
                            "DELAY event requires delayTime, use publish(event, payload, PublishOptions.delay(duration))");
            default -> RocketMQProducerUtil.syncSendNormalMessage(destination, payload);
        }
    }

    @Override
    public void publish(Event event, Object payload, PublishOptions options) {
        var destination = toDestination(event);
        switch (event.getTopicType()) {
            case FIFO ->
                    RocketMQProducerUtil.syncSendFifoMessage(
                            destination, payload, requireMessageGroup(options));
            case DELAY ->
                    RocketMQProducerUtil.syncSendDelayMessage(
                            destination, payload, requireDelayTime(options));
            default -> RocketMQProducerUtil.syncSendNormalMessage(destination, payload);
        }
    }

    // ==================== 异步发布 ====================

    @Override
    public CompletableFuture<Void> asyncPublish(Event event, Object payload) {
        requireNormalEvent(event);
        return RocketMQProducerUtil.asyncSendNormalMessage(toDestination(event), payload)
                .thenAccept(receipt -> {});
    }

    @Override
    public CompletableFuture<Void> asyncPublish(
            Event event, Object payload, AsyncPublishCallback callback) {
        requireNormalEvent(event);
        Objects.requireNonNull(callback, "AsyncPublishCallback cannot be null");
        return RocketMQProducerUtil.asyncSendNormalMessage(toDestination(event), payload)
                .whenComplete(
                        (receipt, ex) -> {
                            callback.onCompleted(event, payload, null, ex);
                        })
                .thenAccept(receipt -> {});
    }

    @Override
    public CompletableFuture<Void> asyncPublish(
            Event event, Object payload, PublishOptions options) {
        var destination = toDestination(event);
        CompletableFuture<SendReceipt> future =
                switch (event.getTopicType()) {
                    case FIFO ->
                            RocketMQProducerUtil.asyncSendFifoMessage(
                                    destination, payload, requireMessageGroup(options));
                    case DELAY ->
                            RocketMQProducerUtil.asyncSendDelayMessage(
                                    destination, payload, requireDelayTime(options));
                    default -> RocketMQProducerUtil.asyncSendNormalMessage(destination, payload);
                };
        return future.thenAccept(receipt -> {});
    }

    @Override
    public CompletableFuture<Void> asyncPublish(
            Event event, Object payload, PublishOptions options, AsyncPublishCallback callback) {
        Objects.requireNonNull(callback, "AsyncPublishCallback cannot be null");
        var destination = toDestination(event);
        CompletableFuture<SendReceipt> future =
                switch (event.getTopicType()) {
                    case FIFO ->
                            RocketMQProducerUtil.asyncSendFifoMessage(
                                    destination, payload, requireMessageGroup(options));
                    case DELAY ->
                            RocketMQProducerUtil.asyncSendDelayMessage(
                                    destination, payload, requireDelayTime(options));
                    default -> RocketMQProducerUtil.asyncSendNormalMessage(destination, payload);
                };
        return future.whenComplete(
                        (receipt, ex) -> callback.onCompleted(event, payload, options, ex))
                .thenAccept(receipt -> {});
    }

    private static void requireNormalEvent(Event event) {
        if (event.getTopicType() != TopicType.NORMAL) {
            throw new IllegalArgumentException(
                    "Only NORMAL events can be published without PublishOptions: " + event);
        }
    }

    private static String requireMessageGroup(PublishOptions options) {
        Objects.requireNonNull(options, "PublishOptions cannot be null for FIFO events");
        String messageGroup = options.getMessageGroup();
        if (messageGroup == null || messageGroup.isBlank()) {
            throw new IllegalArgumentException("messageGroup cannot be blank for FIFO events");
        }
        return messageGroup;
    }

    private static Duration requireDelayTime(PublishOptions options) {
        Objects.requireNonNull(options, "PublishOptions cannot be null for DELAY events");
        Duration delayTime = options.getDelayTime();
        if (delayTime == null || delayTime.isZero() || delayTime.isNegative()) {
            throw new IllegalArgumentException("delayTime must be positive for DELAY events");
        }
        return delayTime;
    }
}
