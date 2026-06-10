package com.travis.infrastructure.common.event;

/**
 * 事件标记接口，定义与底层消息中间件无关的事件契约。
 *
 * <p>业务模块通过枚举实现此接口，每个枚举值代表一个具体的业务事件。 {@link #getTopic()} 和 {@link #getType()} 是事件的逻辑分类标识， 具体如何映射到
 * MQ 的 Topic/Tag 由 {@link MessagePublisher} 的实现类决定。
 *
 * <p>使用示例：
 *
 * <pre>{@code
 * @Getter
 * @AllArgsConstructor
 * public enum SystemEvent implements Event {
 *     USER_LOGIN("system-event", "user-login"),
 *     DEPT_DELETED("system-event", "dept-deleted", TopicType.FIFO);
 *
 *     private final String topic;
 *     private final String type;
 *     private final TopicType topicType;
 *
 *     // 对于 NORMAL 类型的枚举值，可省略 topicType（默认 NORMAL）
 *     SystemEvent(String topic, String type) {
 *         this(topic, type, TopicType.NORMAL);
 *     }
 * }
 * }</pre>
 *
 * <p>发布事件：
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
 * }</pre>
 *
 * @author travis
 * @see MessagePublisher
 * @see PublishOptions
 * @see TopicType
 */
public interface Event {

    /**
     * 获取事件主题（如 {@code system-event}、{@code order-event}）
     *
     * <p>RocketMQ 实现会将其映射为 Topic，Spring Event 实现可忽略。
     *
     * @return 事件主题标识
     */
    String getTopic();

    /**
     * 获取事件类型（如 {@code user-login}、{@code dept-deleted}）
     *
     * <p>RocketMQ 实现会将其映射为 Tag，Spring Event 实现可忽略。
     *
     * @return 事件类型标识
     */
    String getType();

    /**
     * 获取 Topic 类型，用于 Topic 自动创建和消息发送方式选择。
     *
     * <p>默认返回 {@link TopicType#NORMAL}。对于 FIFO 或延迟类型的 Topic， 枚举实现需覆盖此方法或通过构造函数指定。
     *
     * @return Topic 类型
     * @see TopicType
     */
    default TopicType getTopicType() {
        return TopicType.NORMAL;
    }
}
