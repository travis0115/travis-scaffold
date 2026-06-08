package com.travis.infrastructure.framework.rocketmq.core;

import com.travis.infrastructure.common.event.AsyncPublishCallback;
import com.travis.infrastructure.common.event.Event;
import com.travis.infrastructure.common.event.MessagePublisher;
import com.travis.infrastructure.common.event.PublishOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.producer.SendReceipt;

import java.util.concurrent.CompletableFuture;

/**
 * 基于 RocketMQ 的 {@link MessagePublisher} 实现。
 *
 * <p>将 {@link Event} 的 {@code topic} 映射为 Topic，{@code type} 映射为 Tag，
 * 并根据 {@link PublishOptions} 自动选择投递方式：
 *
 * <ul>
 *   <li>{@link PublishOptions.Mode#FIFO} — 顺序消息
 *   <li>{@link PublishOptions.Mode#DELAY} — 延迟消息
 *   <li>其他 — 普通消息
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
        RocketMQProducerUtil.syncSendNormalMessage(toDestination(event), payload);
    }

    @Override
    public void publish(Event event, Object payload, PublishOptions options) {
        var destination = toDestination(event);
        switch (options.getMode()) {
            case FIFO ->
                    RocketMQProducerUtil.syncSendFifoMessage(
                            destination, payload, options.getMessageGroup());
            case DELAY ->
                    RocketMQProducerUtil.syncSendDelayMessage(
                            destination, payload, options.getDelayTime());
            default -> RocketMQProducerUtil.syncSendNormalMessage(destination, payload);
        }
    }

    // ==================== 异步发布 ====================

    @Override
    public CompletableFuture<Void> asyncPublish(Event event, Object payload) {
        return RocketMQProducerUtil
                .asyncSendNormalMessage(toDestination(event), payload)
                .thenAccept(receipt -> {});
    }

    @Override
    public CompletableFuture<Void> asyncPublish(
            Event event, Object payload, AsyncPublishCallback callback) {
        return RocketMQProducerUtil
                .asyncSendNormalMessage(toDestination(event), payload)
                .handle((receipt, ex) -> {
                    callback.onCompleted(event, payload, null, ex);
                    return (Void) null;
                });
    }

    @Override
    public CompletableFuture<Void> asyncPublish(
            Event event, Object payload, PublishOptions options) {
        var destination = toDestination(event);
        CompletableFuture<SendReceipt> future = switch (options.getMode()) {
            case FIFO ->
                    RocketMQProducerUtil.asyncSendFifoMessage(
                            destination, payload, options.getMessageGroup());
            case DELAY ->
                    RocketMQProducerUtil.asyncSendDelayMessage(
                            destination, payload, options.getDelayTime());
            default -> RocketMQProducerUtil.asyncSendNormalMessage(destination, payload);
        };
        return future.thenAccept(receipt -> {});
    }

    @Override
    public CompletableFuture<Void> asyncPublish(
            Event event,
            Object payload,
            PublishOptions options,
            AsyncPublishCallback callback) {
        var destination = toDestination(event);
        CompletableFuture<SendReceipt> future = switch (options.getMode()) {
            case FIFO ->
                    RocketMQProducerUtil.asyncSendFifoMessage(
                            destination, payload, options.getMessageGroup());
            case DELAY ->
                    RocketMQProducerUtil.asyncSendDelayMessage(
                            destination, payload, options.getDelayTime());
            default -> RocketMQProducerUtil.asyncSendNormalMessage(destination, payload);
        };
        return future.handle((receipt, ex) -> {
            callback.onCompleted(event, payload, options, ex);
            return (Void) null;
        });
    }
}
