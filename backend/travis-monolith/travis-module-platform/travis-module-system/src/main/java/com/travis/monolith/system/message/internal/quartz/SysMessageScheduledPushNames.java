package com.travis.monolith.system.message.internal.quartz;

/** 消息定时推送使用的稳定名称。 */
final class SysMessageScheduledPushNames {
    static final String LOCK_NAMESPACE = "system-message";
    static final String RECONCILE_LOCK_KEY = "scheduled-push-reconcile";
    static final String RECONCILE_LOCK_KEY_SPEL = "'" + RECONCILE_LOCK_KEY + "'";
    static final String QUARTZ_GROUP = "system-message";
    static final String QUARTZ_GROUP_OWNER = "system-message-scheduled-push";
    static final String JOB_NAME_PREFIX = "scheduled-message-push-";

    private SysMessageScheduledPushNames() {}
}
