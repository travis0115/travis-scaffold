package com.travis.infrastructure.common.event;

import lombok.Builder;
import lombok.Getter;

import java.time.Duration;

/**
 * 事件发布选项，携带消息投递所需的运行时参数。作为 {@link MessagePublisher} 的运行时入参使用。
 *
 * <p>消息类型（普通、FIFO、延迟、事务）由 {@link Event#getTopicType()} 决定， 本类仅承载运行时动态参数：
 *
 * <ul>
 *   <li>{@link #fifo(String)} — 提供 FIFO 消息的消息组名
 *   <li>{@link #delay(Duration)} — 提供延迟消息的延迟时间
 * </ul>
 *
 * <p>使用示例：
 *
 * <pre>{@code
 * // 普通消息 — 无需 PublishOptions
 * messagePublisher.publish(SystemEvent.USER_LOGIN, payload);
 *
 * // 顺序消息 — 提供 messageGroup
 * messagePublisher.publish(SystemEvent.DEPT_DELETED, payload, PublishOptions.fifo("dept-" + deptId));
 *
 * // 延迟消息 — 提供延迟时间
 * messagePublisher.publish(SystemEvent.USER_LOGIN, payload, PublishOptions.delay(Duration.ofMinutes(30)));
 * }</pre>
 *
 * @author travis
 * @see MessagePublisher
 * @see Event
 * @see TopicType
 */
@Getter
@Builder
public class PublishOptions {

    /** 消息组名，用于 FIFO 顺序消息，运行时动态确定（如订单ID、部门ID） */
    private final String messageGroup;

    /** 延迟时间，用于延迟消息 */
    private final Duration delayTime;

    /**
     * 顺序消息（FIFO），相同 {@code messageGroup} 的消息保证消费顺序
     *
     * @param messageGroup 消息组名，运行时动态传入，如 {@code "dept-" + deptId}
     */
    public static PublishOptions fifo(String messageGroup) {
        return PublishOptions.builder().messageGroup(messageGroup).build();
    }

    /**
     * 延迟消息
     *
     * @param delayTime 延迟时间
     */
    public static PublishOptions delay(Duration delayTime) {
        return PublishOptions.builder().delayTime(delayTime).build();
    }
}
