package com.travis.infrastructure.common.message;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * 消息发布器接口，定义面向消息中间件的发布契约。
 *
 * <p>业务模块通过 {@link Message} 枚举定义需要投递到消息中间件的消息事件，然后通过此接口发布。该抽象用于屏蔽 RocketMQ / Kafka 等 MQ 客户端差异，不作为
 * Spring Modulith 内部模块事件的默认发布入口。
 *
 * <p>单体内部模块解耦事件优先使用 {@code ApplicationEventPublisher} 发布具体事件对象，并通过
 * {@code @ApplicationModuleListener} 按类型监听。只有需要延迟、顺序、跨系统投递等 MQ 能力时，再使用本接口。
 *
 * <p>消息类型由 {@link Message#getMessageType()} 决定：
 *
 * <ul>
 *   <li>{@link MessageType#NORMAL} — 普通消息，直接调用 {@link #publish(Message, Object)}
 *   <li>{@link MessageType#ORDERED} — 顺序消息，直接调用 {@link #publishOrdered(Message, Object, String)}
 *   <li>{@link MessageType#DELAYED} — 延迟消息，直接调用 {@link #publishDelayed(Message, Object, Duration)}
 * </ul>
 *
 * <p>使用示例：
 *
 * <pre>{@code
 * // 普通发布
 * messagePublisher.publish(SystemMessage.USER_LOGIN, payload);
 *
 * // 顺序发布（MessageType.ORDERED 的消息需提供 orderingKey）
 * messagePublisher.publishOrdered(SystemMessage.DEPT_DELETED, payload, "dept-" + deptId);
 *
 * // 延迟发布（MessageType.DELAYED 的消息需提供 delayTime）
 * messagePublisher.publishDelayed(SystemMessage.USER_LOGIN, payload, Duration.ofMinutes(30));
 *
 * // 异步发布（带回调感知发送结果）
 * messagePublisher.asyncPublish(SystemMessage.USER_LOGIN, payload,
 *         (message, p, options, ex) -> {
 *             if (ex != null) {
 *                 log.error("发送失败, message={}, payload={}", message, p, ex);
 *             } else {
 *                 log.info("发送成功, message={}", message);
 *             }
 *         });
 * }</pre>
 *
 * @author travis
 * @see Message
 * @see MessagePublishOptions
 * @see MessageType
 */
public interface MessagePublisher {

    /**
     * 同步发布消息（普通模式）
     *
     * @param message 消息枚举
     * @param payload 消息体
     */
    void publish(Message message, Object payload);

    /**
     * 同步发布消息（指定发布选项，如顺序或延迟）
     *
     * @param message 消息枚举
     * @param payload 消息体
     * @param options 发布选项
     */
    void publish(Message message, Object payload, MessagePublishOptions options);

    /**
     * 同步发布顺序消息。
     *
     * @param message 消息枚举
     * @param payload 消息体
     * @param orderingKey 排序键，相同键的消息保证消费顺序
     */
    default void publishOrdered(Message message, Object payload, String orderingKey) {
        publish(message, payload, MessagePublishOptions.ordered(orderingKey));
    }

    /**
     * 同步发布延迟消息。
     *
     * @param message 消息枚举
     * @param payload 消息体
     * @param delayTime 延迟时间
     */
    default void publishDelayed(Message message, Object payload, Duration delayTime) {
        publish(message, payload, MessagePublishOptions.delayed(delayTime));
    }

    /**
     * 异步发布消息（普通模式）
     *
     * @param message 消息枚举
     * @param payload 消息体
     * @return 异步结果
     */
    CompletableFuture<Void> asyncPublish(Message message, Object payload);

    /**
     * 异步发布消息（普通模式，带回调）
     *
     * <p>发送完成后调用 {@code callback}，成功时 {@code ex} 为 {@code null}，失败时携带异常。 回调参数携带完整的发布上下文，便于日志记录或重试。
     *
     * @param message 消息枚举
     * @param payload 消息体
     * @param callback 发送完成回调
     * @return 异步结果
     * @see MessagePublishCallback#onCompleted(Message, Object, MessagePublishOptions, Throwable)
     */
    CompletableFuture<Void> asyncPublish(
            Message message, Object payload, MessagePublishCallback callback);

    /**
     * 异步发布消息（指定发布选项）
     *
     * @param message 消息枚举
     * @param payload 消息体
     * @param options 发布选项
     * @return 异步结果
     */
    CompletableFuture<Void> asyncPublish(
            Message message, Object payload, MessagePublishOptions options);

    /**
     * 异步发布顺序消息。
     *
     * @param message 消息枚举
     * @param payload 消息体
     * @param orderingKey 排序键，相同键的消息保证消费顺序
     * @return 异步结果
     */
    default CompletableFuture<Void> asyncPublishOrdered(
            Message message, Object payload, String orderingKey) {
        return asyncPublish(message, payload, MessagePublishOptions.ordered(orderingKey));
    }

    /**
     * 异步发布延迟消息。
     *
     * @param message 消息枚举
     * @param payload 消息体
     * @param delayTime 延迟时间
     * @return 异步结果
     */
    default CompletableFuture<Void> asyncPublishDelayed(
            Message message, Object payload, Duration delayTime) {
        return asyncPublish(message, payload, MessagePublishOptions.delayed(delayTime));
    }

    /**
     * 异步发布顺序消息（带回调）。
     *
     * @param message 消息枚举
     * @param payload 消息体
     * @param orderingKey 排序键，相同键的消息保证消费顺序
     * @param callback 发送完成回调
     * @return 异步结果
     */
    default CompletableFuture<Void> asyncPublishOrdered(
            Message message, Object payload, String orderingKey, MessagePublishCallback callback) {
        return asyncPublish(message, payload, MessagePublishOptions.ordered(orderingKey), callback);
    }

    /**
     * 异步发布延迟消息（带回调）。
     *
     * @param message 消息枚举
     * @param payload 消息体
     * @param delayTime 延迟时间
     * @param callback 发送完成回调
     * @return 异步结果
     */
    default CompletableFuture<Void> asyncPublishDelayed(
            Message message, Object payload, Duration delayTime, MessagePublishCallback callback) {
        return asyncPublish(message, payload, MessagePublishOptions.delayed(delayTime), callback);
    }

    /**
     * 异步发布消息（指定发布选项，带回调）
     *
     * <p>发送完成后调用 {@code callback}，成功时 {@code ex} 为 {@code null}，失败时携带异常。 回调参数携带完整的发布上下文，便于日志记录或重试。
     *
     * @param message 消息枚举
     * @param payload 消息体
     * @param options 发布选项
     * @param callback 发送完成回调
     * @return 异步结果
     * @see MessagePublishCallback#onCompleted(Message, Object, MessagePublishOptions, Throwable)
     */
    CompletableFuture<Void> asyncPublish(
            Message message,
            Object payload,
            MessagePublishOptions options,
            MessagePublishCallback callback);
}
