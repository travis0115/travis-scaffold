package com.travis.infrastructure.common.message;

/**
 * 消息投递类型枚举，定义业务侧需要的 MQ 投递语义。
 *
 * <p>由 {@link Message#getMessageType()} 返回，用于：
 *
 * <ul>
 *   <li>MQ Topic 自动创建时确定底层 Topic 类型（如 RocketMQ 的 {@code TopicMessageType}）
 *   <li>{@link MessagePublisher} 根据类型选择正确的发送方式（普通、顺序、延迟）
 * </ul>
 *
 * <p>使用示例：
 *
 * <pre>{@code
 * @Getter
 * @AllArgsConstructor
 * public enum OrderMessage implements Message {
 *     ORDER_CREATED("order-event", "order-created", MessageType.ORDERED),
 *     ORDER_PAID("order-event", "order-paid", MessageType.DELAYED);
 *
 *     private final String topic;
 *     private final String type;
 *     private final MessageType messageType;
 * }
 * }</pre>
 *
 * @author travis
 * @see Message
 */
public enum MessageType {

    /** 普通消息（默认） */
    NORMAL,

    /** 顺序消息，需配合 orderingKey 保证同一业务键下的消费顺序 */
    ORDERED,

    /** 延迟消息，需配合 delayTime 提供延迟时间 */
    DELAYED
}
