package com.travis.monolith.system.message.internal.service.impl;

import com.travis.infrastructure.framework.redis.core.util.RedisUtil;
import com.travis.monolith.system.message.api.enums.SysMessageReceiverScope;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** 使用版本键维护未读数缓存，避免消息发布时扫描并清空全部用户缓存。 */
@Component
public class SysMessageInboxCache {
    private static final String KEY_PREFIX = "system:message:inbox:unread:";
    private static final long UNREAD_TTL_MILLIS = TimeUnit.MINUTES.toMillis(10);
    private static final long VERSION_TTL_MILLIS = TimeUnit.DAYS.toMillis(30);
    private static final int PRECISE_INVALIDATION_LIMIT = 100;

    /** 查询指定用户的未读数缓存。 */
    public Lookup lookup(String receiverType, Long userId, List<Long> roleIds, Long deptId) {
        String key = dataKey(receiverType, userId, roleIds, deptId);
        Object value = RedisUtil.get(key);
        if (value instanceof Number number) {
            return new Lookup(key, number.longValue());
        }
        return new Lookup(key, value == null ? null : Long.valueOf(value.toString()));
    }

    /** 写入指定用户的未读数缓存。 */
    public void put(Lookup lookup, Long count) {
        RedisUtil.set(lookup.key(), count, UNREAD_TTL_MILLIS);
    }

    public void invalidateReceiver(String receiverType) {
        runAfterCommit(() -> incrementVersion(receiverVersionKey(receiverType)));
    }

    public void invalidateUser(String receiverType, Long userId) {
        runAfterCommit(() -> incrementVersion(userVersionKey(receiverType, userId)));
    }

    /** 按消息接收范围精准失效；接收对象过多时退化为接收端版本失效。 */
    public void invalidateAudience(
            String receiverType, Integer receiverScope, List<Long> receiverValues) {
        if (SysMessageReceiverScope.ALL.getValue().equals(receiverScope)
                || receiverValues == null
                || receiverValues.size() > PRECISE_INVALIDATION_LIMIT) {
            invalidateReceiver(receiverType);
            return;
        }
        runAfterCommit(
                () ->
                        receiverValues.forEach(
                                value ->
                                        incrementVersion(
                                                audienceVersionKey(
                                                        receiverType, receiverScope, value))));
    }

    private String dataKey(String receiverType, Long userId, List<Long> roleIds, Long deptId) {
        List<Long> sortedRoleIds =
                roleIds == null
                        ? List.of()
                        : roleIds.stream().sorted(Comparator.naturalOrder()).toList();
        List<String> versionKeys = new ArrayList<>(3 + sortedRoleIds.size());
        versionKeys.add(receiverVersionKey(receiverType));
        versionKeys.add(userVersionKey(receiverType, userId));
        versionKeys.add(
                audienceVersionKey(receiverType, SysMessageReceiverScope.USER.getValue(), userId));
        sortedRoleIds.forEach(
                roleId ->
                        versionKeys.add(
                                audienceVersionKey(
                                        receiverType,
                                        SysMessageReceiverScope.ROLE.getValue(),
                                        roleId)));
        if (deptId != null) {
            versionKeys.add(
                    audienceVersionKey(
                            receiverType, SysMessageReceiverScope.DEPT.getValue(), deptId));
        }
        List<Object> versions = RedisUtil.multiGet(versionKeys);
        int versionIndex = 0;
        var key =
                new StringBuilder(
                        KEY_PREFIX
                                + "data:"
                                + receiverType
                                + ':'
                                + version(versions.get(versionIndex++))
                                + ':'
                                + userId
                                + ':'
                                + version(versions.get(versionIndex++)));
        key.append(":user.").append(version(versions.get(versionIndex++)));
        for (Long roleId : sortedRoleIds) {
            key.append(":role.")
                    .append(roleId)
                    .append('.')
                    .append(version(versions.get(versionIndex++)));
        }
        if (deptId != null) {
            key.append(":dept.")
                    .append(deptId)
                    .append('.')
                    .append(version(versions.get(versionIndex)));
        }
        return key.toString();
    }

    private String receiverVersionKey(String receiverType) {
        return KEY_PREFIX + "version:" + receiverType;
    }

    private String userVersionKey(String receiverType, Long userId) {
        return KEY_PREFIX + "version:" + receiverType + ':' + userId;
    }

    private String audienceVersionKey(
            String receiverType, Integer receiverScope, Long receiverValue) {
        return KEY_PREFIX
                + "audience-version:"
                + receiverType
                + ':'
                + receiverScope
                + ':'
                + receiverValue;
    }

    private long version(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return value == null ? 0L : Long.parseLong(value.toString());
    }

    private void incrementVersion(String key) {
        RedisUtil.increment(key, 1);
        RedisUtil.setExpire(key, VERSION_TTL_MILLIS);
    }

    private void runAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            task.run();
                        }
                    });
            return;
        }
        task.run();
    }

    record Lookup(String key, Long value) {}
}
