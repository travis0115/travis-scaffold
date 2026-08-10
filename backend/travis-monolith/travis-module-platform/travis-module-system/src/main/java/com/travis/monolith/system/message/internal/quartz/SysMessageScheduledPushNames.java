package com.travis.monolith.system.message.internal.quartz;

/** 消息定时推送使用的稳定名称。 */
final class SysMessageScheduledPushNames {
    /** 消息定时推送使用的分布式锁命名空间。 */
    static final String LOCK_NAMESPACE = "system-message";

    /** 定时推送对账任务使用的分布式锁键。 */
    static final String RECONCILE_LOCK_KEY = "scheduled-push-reconcile";

    /** 供 {@code @DistributedLock} 注解使用的对账锁键 SpEL 表达式。 */
    static final String RECONCILE_LOCK_KEY_SPEL = "'" + RECONCILE_LOCK_KEY + "'";

    /** 消息定时推送任务所属的 Quartz 分组。 */
    static final String QUARTZ_GROUP = "system-message";

    /** 消息定时推送 Quartz 分组的注册所有者。 */
    static final String QUARTZ_GROUP_OWNER = "system-message-scheduled-push";

    /** 消息定时推送 Quartz 任务的名称前缀。 */
    static final String JOB_NAME_PREFIX = "scheduled-message-push-";

    private SysMessageScheduledPushNames() {}
}
