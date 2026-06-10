package com.travis.monolith.system.common.api;

import com.travis.infrastructure.common.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * System 模块事件定义
 *
 * <p>每个枚举值代表一个具体的业务事件。业务层通过 {@code messagePublisher.publish(SystemEvent.USER_LOGIN, payload)} 发布事件，
 * 无需关心底层 MQ 的 Topic/Tag 细节。
 *
 * <p>消费端通过 {@link #SystemEvent}、各 {@code *_TAG} 和 {@code *_GROUP} 常量配置
 * {@code @RocketMQMessageListener} 注解， 确保发布端与消费端引用同一来源，避免魔法值。
 *
 * <p>如需 FIFO 顺序或延迟投递，在发布时传入 {@link com.travis.infrastructure.common.event.PublishOptions}：
 *
 * <pre>{@code
 * // FIFO：按部门ID保序
 * messagePublisher.publish(DEPT_DELETED, payload, PublishOptions.fifo("dept-" + deptId));
 *
 * // 延迟30分钟
 * messagePublisher.publish(USER_LOGIN, payload, PublishOptions.delay(Duration.ofMinutes(30)));
 * }</pre>
 *
 * @author travis
 */
@Getter
@AllArgsConstructor
public enum SystemEvent implements Event {

    /** 用户登录事件 */
    USER_LOGIN(SystemEventConstant.TOPIC, SystemEventConstant.USER_LOGIN_TAG),

    /** 部门删除事件 */
    DEPT_DELETED(SystemEventConstant.TOPIC, SystemEventConstant.DEPT_DELETED_TAG);

    private final String topic;
    private final String type;
}
