package com.travis.infrastructure.common.event;

/**
 * Topic 类型枚举，定义与底层消息中间件无关的消息语义分类。
 *
 * <p>由 {@link Event#getTopicType()} 返回，用于：
 *
 * <ul>
 *   <li>Topic 自动创建时确定 Topic 类型（如 RocketMQ 的 {@code TopicMessageType}）
 *   <li>Publisher 根据类型选择正确的发送方式（普通、顺序、延迟）
 * </ul>
 *
 * <p>使用示例：
 *
 * <pre>{@code
 * @Getter
 * @AllArgsConstructor
 * public enum OrderEvent implements Event {
 *     ORDER_CREATED("order-event", "order-created", TopicType.FIFO),
 *     ORDER_PAID("order-event", "order-paid", TopicType.DELAY);
 *
 *     private final String topic;
 *     private final String type;
 *     private final TopicType topicType;
 * }
 * }</pre>
 *
 * @author travis
 * @see Event
 */
public enum TopicType {

    /** 普通消息（默认） */
    NORMAL,

    /** 顺序消息（FIFO），需配合 {@link PublishOptions#fifo(String)} 提供消息组 */
    FIFO,

    /** 延迟消息，需配合 {@link PublishOptions#delay(java.time.Duration)} 提供延迟时间 */
    DELAY
}
