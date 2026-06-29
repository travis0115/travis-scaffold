package com.travis.infrastructure.framework.rocketmq.core;

import com.travis.infrastructure.common.message.*;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

/**
 * 基于 RocketMQ 的 {@link MessagePublisher} 实现。
 *
 * <p>将 {@link Message} 的 {@code topic} 映射为 Topic，{@code type} 映射为 Tag， 并根据 {@link
 * Message#getMessageType()} 自动选择投递方式：
 *
 * <ul>
 *   <li>{@link MessageType#NORMAL} — 普通消息
 *   <li>{@link MessageType#ORDERED} — 顺序消息，需通过 {@link MessagePublishOptions#ordered(String)} 提供
 *       orderingKey
 *   <li>{@link MessageType#DELAYED} — 延迟消息，需通过 {@link
 *       MessagePublishOptions#delayed(java.time.Duration)} 提供 delayTime
 * </ul>
 *
 * @author travis
 * @see MessagePublisher
 * @see Message
 */
@Slf4j
public class RocketMQMessagePublisher implements MessagePublisher {

    /** 将 Message 解析为 RocketMQ destination 格式：{@code topic:type} */
    private static String toDestination(Message message) {
        return message.getTopic() + ":" + message.getType();
    }

    // ==================== 同步发布 ====================

    @Override
    public void publish(Message message, Object payload) {
        requireNormalMessage(message);
        RocketMQProducerUtil.syncSendNormalMessage(toDestination(message), payload);
    }

    @Override
    public void publish(Message message, Object payload, MessagePublishOptions options) {
        var destination = toDestination(message);
        switch (message.getMessageType()) {
            case ORDERED ->
                    RocketMQProducerUtil.syncSendFifoMessage(
                            destination, payload, requireOrderingKey(options));
            case DELAYED ->
                    RocketMQProducerUtil.syncSendDelayMessage(
                            destination, payload, requireDelayTime(options));
            default -> RocketMQProducerUtil.syncSendNormalMessage(destination, payload);
        }
    }

    // ==================== 异步发布 ====================

    @Override
    public CompletableFuture<Void> asyncPublish(Message message, Object payload) {
        requireNormalMessage(message);
        return RocketMQProducerUtil.asyncSendNormalMessage(toDestination(message), payload)
                .thenAccept(_ -> {});
    }

    @Override
    public CompletableFuture<Void> asyncPublish(
            Message message, Object payload, MessagePublishCallback callback) {
        requireNormalMessage(message);
        Objects.requireNonNull(callback, "MessagePublishCallback cannot be null");
        return RocketMQProducerUtil.asyncSendNormalMessage(toDestination(message), payload)
                .whenComplete(
                        (_, ex) -> {
                            callback.onCompleted(message, payload, null, ex);
                        })
                .thenAccept(_ -> {});
    }

    @Override
    public CompletableFuture<Void> asyncPublish(
            Message message, Object payload, MessagePublishOptions options) {
        var destination = toDestination(message);
        var future =
                switch (message.getMessageType()) {
                    case ORDERED ->
                            RocketMQProducerUtil.asyncSendFifoMessage(
                                    destination, payload, requireOrderingKey(options));
                    case DELAYED ->
                            RocketMQProducerUtil.asyncSendDelayMessage(
                                    destination, payload, requireDelayTime(options));
                    default -> RocketMQProducerUtil.asyncSendNormalMessage(destination, payload);
                };
        return future.thenAccept(receipt -> {});
    }

    @Override
    public CompletableFuture<Void> asyncPublish(
            Message message,
            Object payload,
            MessagePublishOptions options,
            MessagePublishCallback callback) {
        Objects.requireNonNull(callback, "MessagePublishCallback cannot be null");
        var destination = toDestination(message);
        var future =
                switch (message.getMessageType()) {
                    case ORDERED ->
                            RocketMQProducerUtil.asyncSendFifoMessage(
                                    destination, payload, requireOrderingKey(options));
                    case DELAYED ->
                            RocketMQProducerUtil.asyncSendDelayMessage(
                                    destination, payload, requireDelayTime(options));
                    default -> RocketMQProducerUtil.asyncSendNormalMessage(destination, payload);
                };
        return future.whenComplete(
                        (receipt, ex) -> callback.onCompleted(message, payload, options, ex))
                .thenAccept(receipt -> {});
    }

    private static void requireNormalMessage(Message message) {
        if (message.getMessageType() != MessageType.NORMAL) {
            throw new IllegalArgumentException(
                    "Only NORMAL messages can be published without MessagePublishOptions: "
                            + message);
        }
    }

    private static String requireOrderingKey(MessagePublishOptions options) {
        Objects.requireNonNull(
                options, "MessagePublishOptions cannot be null for ordered messages");
        String orderingKey = options.getOrderingKey();
        if (orderingKey == null || orderingKey.isBlank()) {
            throw new IllegalArgumentException("orderingKey cannot be blank for ordered messages");
        }
        return orderingKey;
    }

    private static Duration requireDelayTime(MessagePublishOptions options) {
        Objects.requireNonNull(
                options, "MessagePublishOptions cannot be null for delayed messages");
        Duration delayTime = options.getDelayTime();
        if (delayTime == null || delayTime.isZero() || delayTime.isNegative()) {
            throw new IllegalArgumentException("delayTime must be positive for delayed messages");
        }
        return delayTime;
    }
}
