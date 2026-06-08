package com.travis.infrastructure.common.event;

/**
 * 事件标记接口，定义与底层消息中间件无关的事件契约。
 *
 * <p>业务模块通过枚举实现此接口，每个枚举值代表一个具体的业务事件。
 * {@link #getTopic()} 和 {@link #getType()} 是事件的逻辑分类标识，
 * 具体如何映射到 MQ 的 Topic/Tag 由 {@link MessagePublisher} 的实现类决定。
 *
 * <p>使用示例：
 *
 * <pre>{@code
 * @Getter
 * @AllArgsConstructor
 * public enum SystemEvent implements Event {
 *     USER_LOGIN("system-event", "user-login"),
 *     DEPT_DELETED("system-event", "dept-deleted");
 *
 *     private final String topic;
 *     private final String type;
 * }
 * }</pre>
 *
 * <p>发布事件：
 *
 * <pre>{@code
 * // 普通发布
 * messagePublisher.publish(SystemEvent.USER_LOGIN, payload);
 *
 * // FIFO 顺序发布（messageGroup 运行时动态传入）
 * messagePublisher.publish(SystemEvent.DEPT_DELETED, payload, PublishOptions.fifo("dept-" + deptId));
 *
 * // 延迟发布
 * messagePublisher.publish(SystemEvent.USER_LOGIN, payload, PublishOptions.delay(Duration.ofMinutes(30)));
 * }</pre>
 *
 * @author travis
 * @see MessagePublisher
 * @see PublishOptions
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
}
