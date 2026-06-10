package com.travis.monolith.system.user.internal.event;

/**
 * 事件消费者组
 *
 * @author Travis
 */
public class EventConsumerGroup {
    /** 部门删除事件消费者组 */
    public static final String DEPT_DELETED_CONSUMER_GROUP = "dept-deleted-consumer-group";

    /** 用户登录事件消费者组 */
    public static final String USER_LOGIN_CONSUMER_GROUP = "user-login-consumer-group";
}
