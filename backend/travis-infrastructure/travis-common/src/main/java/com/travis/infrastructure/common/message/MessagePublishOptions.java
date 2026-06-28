package com.travis.infrastructure.common.message;

import lombok.Builder;
import lombok.Getter;

import java.time.Duration;

/**
 * 消息发布选项，携带 MQ 投递所需的运行时参数。作为 {@link MessagePublisher} 的运行时入参使用。
 *
 * <p>消息类型（普通、顺序、延迟）由 {@link Message#getMessageType()} 决定， 本类仅承载运行时动态参数：
 *
 * <ul>
 *   <li>{@link #ordered(String)} — 提供顺序消息的排序键
 *   <li>{@link #delayed(Duration)} — 提供延迟消息的延迟时间
 * </ul>
 *
 * <p>使用示例：
 *
 * <pre>{@code
 * // 普通消息 — 无需 MessagePublishOptions
 * messagePublisher.publish(SystemMessage.USER_LOGIN, payload);
 *
 * // 顺序消息 — 提供 orderingKey
 * messagePublisher.publish(SystemMessage.DEPT_DELETED, payload, MessagePublishOptions.ordered("dept-" + deptId));
 *
 * // 延迟消息 — 提供延迟时间
 * messagePublisher.publish(SystemMessage.USER_LOGIN, payload, MessagePublishOptions.delayed(Duration.ofMinutes(30)));
 * }</pre>
 *
 * @author travis
 * @see MessagePublisher
 * @see Message
 * @see MessageType
 */
@Getter
@Builder
public class MessagePublishOptions {

    /** 排序键，用于顺序消息，运行时动态确定（如订单ID、部门ID） */
    private final String orderingKey;

    /** 延迟时间，用于延迟消息 */
    private final Duration delayTime;

    /**
     * 顺序消息，相同 {@code orderingKey} 的消息保证消费顺序
     *
     * @param orderingKey 排序键，运行时动态传入，如 {@code "dept-" + deptId}
     */
    public static MessagePublishOptions ordered(String orderingKey) {
        return MessagePublishOptions.builder().orderingKey(orderingKey).build();
    }

    /**
     * 延迟消息
     *
     * @param delayTime 延迟时间
     */
    public static MessagePublishOptions delayed(Duration delayTime) {
        return MessagePublishOptions.builder().delayTime(delayTime).build();
    }
}
