package com.travis.infrastructure.common.event;

import lombok.Builder;
import lombok.Getter;

import java.time.Duration;

/**
 * 事件发布选项，控制消息的投递方式。作为 {@link MessagePublisher} 的运行时入参使用。
 *
 * <p>支持三种模式：
 *
 * <ul>
 *   <li>{@link Mode#NORMAL} — 普通消息（默认，无需传 options）
 *   <li>{@link Mode#FIFO} — 顺序消息，需动态传入 {@code messageGroup}
 *   <li>{@link Mode#DELAY} — 延迟消息，需指定 {@code delayTime}
 * </ul>
 *
 * <p>使用示例：
 *
 * <pre>{@code
 * // 普通消息 — 无需 PublishOptions
 * messagePublisher.publish(SystemEvent.USER_LOGIN, payload);
 *
 * // 顺序消息 — messageGroup 运行时动态传入
 * messagePublisher.publish(SystemEvent.DEPT_DELETED, payload, PublishOptions.fifo("dept-" + deptId));
 *
 * // 延迟消息
 * messagePublisher.publish(SystemEvent.USER_LOGIN, payload, PublishOptions.delay(Duration.ofMinutes(30)));
 * }</pre>
 *
 * @author travis
 * @see MessagePublisher
 * @see Event
 */
@Getter
@Builder
public class PublishOptions {

    /** 投递模式 */
    private final Mode mode;

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
        return PublishOptions.builder().mode(Mode.FIFO).messageGroup(messageGroup).build();
    }

    /**
     * 延迟消息
     *
     * @param delayTime 延迟时间
     */
    public static PublishOptions delay(Duration delayTime) {
        return PublishOptions.builder().mode(Mode.DELAY).delayTime(delayTime).build();
    }

    /** 投递模式 */
    public enum Mode {
        /** 普通消息 */
        NORMAL,
        /** 顺序消息（FIFO） */
        FIFO,
        /** 延迟消息 */
        DELAY
    }
}
