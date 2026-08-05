package com.travis.monolith.system.message.internal.quartz;

/** 消息定时推送使用的稳定名称。 */
final class SysMessageScheduledPushNames {
    static final String LOCK_NAMESPACE = "system-message";
    static final String RECONCILE_LOCK_KEY_SPEL = "'scheduled-push-reconcile'";
    static final String QUARTZ_GROUP = "system-message";
    static final String JOB_NAME_PREFIX = "scheduled-message-push-";
    static final String TRIGGER_NAME_PREFIX = "scheduled-message-push-trigger-";
    static final String LEGACY_JOB_NAME = "scheduled-message-push";
    static final String RECONCILE_SLOT_KEY_PREFIX = "system:message:scheduled-push:reconcile:";

    private SysMessageScheduledPushNames() {}
}
