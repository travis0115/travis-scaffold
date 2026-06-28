package com.travis.infrastructure.common.message;

/**
 * 消息事件标记接口，定义面向消息中间件的发布契约。
 *
 * <p>业务模块通过枚举实现此接口，每个枚举值代表一个需要投递到消息中间件的消息事件。 {@link #getTopic()} 和 {@link #getType()} 是消息路由标识，通常由
 * MQ 实现映射为 Topic/Tag。
 *
 * <p>单体内部模块解耦事件优先使用 Spring Modulith 的类型事件模型，直接发布具体事件对象；只有需要延迟、顺序、跨系统投递等消息中间件能力时，再使用本接口。
 *
 * <p>使用示例：
 *
 * <pre>{@code
 * @Getter
 * @AllArgsConstructor
 * public enum SystemMessage implements Message {
 *     USER_LOGIN("system-event", "user-login"),
 *     DEPT_DELETED("system-event", "dept-deleted", MessageType.ORDERED);
 *
 *     private final String topic;
 *     private final String type;
 *     private final MessageType messageType;
 *
 *     // 对于 NORMAL 类型的枚举值，可省略 MessageType（默认 NORMAL）
 *     SystemMessage(String topic, String type) {
 *         this(topic, type, MessageType.NORMAL);
 *     }
 * }
 * }</pre>
 *
 * <p>发布消息：
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
 * }</pre>
 *
 * @author travis
 * @see MessagePublisher
 * @see MessagePublishOptions
 * @see MessageType
 */
public interface Message {

    /**
     * 获取消息主题（如 {@code system-event}、{@code order-event}）
     *
     * <p>RocketMQ 实现会将其映射为 Topic。
     *
     * @return 消息主题标识
     */
    String getTopic();

    /**
     * 获取消息类型（如 {@code user-login}、{@code dept-deleted}）
     *
     * <p>RocketMQ 实现会将其映射为 Tag。
     *
     * @return 消息类型标识
     */
    String getType();

    /**
     * 获取消息投递类型，用于 MQ Topic 自动创建和消息发送方式选择。
     *
     * <p>默认返回 {@link MessageType#NORMAL}。对于顺序或延迟消息，枚举实现需覆盖此方法或通过构造函数指定。
     *
     * @return 消息投递类型
     * @see MessageType
     */
    default MessageType getMessageType() {
        return MessageType.NORMAL;
    }
}
