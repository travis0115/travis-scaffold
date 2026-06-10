package com.travis.infrastructure.common.event;

import java.util.concurrent.CompletableFuture;

/**
 * 消息发布器接口，定义与底层消息中间件无关的事件发布契约。
 *
 * <p>业务模块通过 {@link Event} 枚举定义事件，然后通过此接口发布。
 * 切换底层实现（RocketMQ / Kafka / Spring Event / Spring Modulith）只需更换实现类，
 * 业务枚举和调用代码无需任何变动。
 *
 * <p>消息类型由 {@link Event#getTopicType()} 决定：
 *
 * <ul>
 *   <li>{@link TopicType#NORMAL} — 普通消息，直接调用 {@link #publish(Event, Object)}
 *   <li>{@link TopicType#FIFO} — 顺序消息，需提供 {@link PublishOptions#fifo(String)}
 *   <li>{@link TopicType#DELAY} — 延迟消息，需提供 {@link PublishOptions#delay(Duration)}
 * </ul>
 *
 * <p>使用示例：
 *
 * <pre>{@code
 * // 普通发布
 * messagePublisher.publish(SystemEvent.USER_LOGIN, payload);
 *
 * // FIFO 顺序发布（TopicType.FIFO 的事件需提供 messageGroup）
 * messagePublisher.publish(SystemEvent.DEPT_DELETED, payload, PublishOptions.fifo("dept-" + deptId));
 *
 * // 延迟发布（TopicType.DELAY 的事件需提供 delayTime）
 * messagePublisher.publish(SystemEvent.USER_LOGIN, payload, PublishOptions.delay(Duration.ofMinutes(30)));
 *
 * // 异步发布（带回调感知发送结果）
 * messagePublisher.asyncPublish(SystemEvent.USER_LOGIN, payload,
 *         (event, p, options, ex) -> {
 *             if (ex != null) {
 *                 log.error("发送失败, event={}, payload={}", event, p, ex);
 *             } else {
 *                 log.info("发送成功, event={}", event);
 *             }
 *         });
 * }</pre>
 *
 * @author travis
 * @see Event
 * @see PublishOptions
 * @see TopicType
 */
public interface MessagePublisher {

    /**
     * 同步发布事件（普通模式）
     *
     * @param event 事件枚举
     * @param payload 消息体
     */
    void publish(Event event, Object payload);

    /**
     * 同步发布事件（指定发布选项，如 FIFO 顺序或延迟）
     *
     * @param event 事件枚举
     * @param payload 消息体
     * @param options 发布选项
     */
    void publish(Event event, Object payload, PublishOptions options);

    /**
     * 异步发布事件（普通模式）
     *
     * @param event 事件枚举
     * @param payload 消息体
     * @return 异步结果
     */
    CompletableFuture<Void> asyncPublish(Event event, Object payload);

    /**
     * 异步发布事件（普通模式，带回调）
     *
     * <p>发送完成后调用 {@code callback}，成功时 {@code ex} 为 {@code null}，失败时携带异常。
     * 回调参数携带完整的发布上下文，便于日志记录或重试。
     *
     * @param event 事件枚举
     * @param payload 消息体
     * @param callback 发送完成回调
     * @return 异步结果
     * @see AsyncPublishCallback#onCompleted(Event, Object, PublishOptions, Throwable)
     */
    CompletableFuture<Void> asyncPublish(Event event, Object payload, AsyncPublishCallback callback);

    /**
     * 异步发布事件（指定发布选项）
     *
     * @param event 事件枚举
     * @param payload 消息体
     * @param options 发布选项
     * @return 异步结果
     */
    CompletableFuture<Void> asyncPublish(Event event, Object payload, PublishOptions options);

    /**
     * 异步发布事件（指定发布选项，带回调）
     *
     * <p>发送完成后调用 {@code callback}，成功时 {@code ex} 为 {@code null}，失败时携带异常。
     * 回调参数携带完整的发布上下文，便于日志记录或重试。
     *
     * @param event 事件枚举
     * @param payload 消息体
     * @param options 发布选项
     * @param callback 发送完成回调
     * @return 异步结果
     * @see AsyncPublishCallback#onCompleted(Event, Object, PublishOptions, Throwable)
     */
    CompletableFuture<Void> asyncPublish(
            Event event, Object payload, PublishOptions options, AsyncPublishCallback callback);
}
