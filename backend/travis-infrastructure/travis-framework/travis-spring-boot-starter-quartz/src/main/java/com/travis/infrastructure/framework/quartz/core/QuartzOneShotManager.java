package com.travis.infrastructure.framework.quartz.core;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;

/** 负责一次性 Quartz 任务的幂等创建、重排、删除和孤儿清理。 */
public class QuartzOneShotManager {

    private static final String TRIGGER_SUFFIX = "-trigger";

    private final Scheduler scheduler;
    private final ConcurrentMap<String, String> groupOwners = new ConcurrentHashMap<>();

    public QuartzOneShotManager(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /** 注册一次性任务分组的唯一业务所有者；同一分组不能由多个业务共同对账。 */
    public void registerGroup(String group, String owner) {
        validateText(group, "Quartz 任务分组不能为空");
        validateText(owner, "Quartz 任务分组所有者不能为空");
        String existingOwner = groupOwners.putIfAbsent(group, owner);
        if (existingOwner != null && !existingOwner.equals(owner)) {
            throw new IllegalStateException(
                    "Quartz 任务分组已被其他业务注册: " + group + " (" + existingOwner + ")");
        }
    }

    /** 使 Quartz 中的任务与期望状态一致。 */
    public void sync(QuartzOneShotTask task) {
        Objects.requireNonNull(task, "Quartz 一次性任务不能为空");
        requireGroupRegistered(task.group());
        try {
            if (isCurrent(task)) {
                return;
            }
            var jobDetail =
                    JobBuilder.newJob(task.jobClass())
                            .withIdentity(jobKey(task))
                            .withDescription(task.description())
                            .usingJobData(new JobDataMap(task.data()))
                            .storeDurably()
                            .build();
            scheduler.addJob(jobDetail, true);

            Trigger trigger = buildTrigger(task);
            if (scheduler.rescheduleJob(trigger.getKey(), trigger) == null) {
                try {
                    scheduler.scheduleJob(trigger);
                } catch (ObjectAlreadyExistsException exception) {
                    if (scheduler.rescheduleJob(trigger.getKey(), trigger) == null) {
                        throw exception;
                    }
                }
            }
            removeUnexpectedTriggers(jobKey(task), trigger.getKey());
        } catch (Exception exception) {
            throw operationError(
                    "同步一次性 Quartz 任务失败: " + task.group() + '/' + task.taskName(), exception);
        }
    }

    /** 删除指定的一次性任务。 */
    public void delete(String group, String taskName) {
        requireGroupRegistered(group);
        try {
            scheduler.deleteJob(jobKey(group, taskName));
        } catch (Exception exception) {
            throw operationError("删除一次性 Quartz 任务失败: " + group + '/' + taskName, exception);
        }
    }

    /** 对账指定业务分组：同步期望任务，并删除不再被业务期望的孤儿任务。 */
    public void reconcile(String group, String owner, QuartzOneShotTaskScanner taskScanner) {
        requireGroupOwner(group, owner);
        Objects.requireNonNull(taskScanner, "Quartz 一次性任务扫描器不能为空");
        Set<String> expectedTaskNames = new HashSet<>();
        taskScanner.scan(
                task -> {
                    if (!group.equals(task.group())) {
                        throw new IllegalArgumentException("Quartz 一次性任务分组不匹配: " + task.group());
                    }
                    expectedTaskNames.add(task.taskName());
                    sync(task);
                });
        deleteUnexpected(group, expectedTaskNames);
    }

    /** 删除指定分组中不再被业务期望的任务。 */
    private void deleteUnexpected(String group, Set<String> expectedTaskNames) {
        try {
            Set<JobKey> expectedJobKeys =
                    expectedTaskNames.stream()
                            .map(taskName -> jobKey(group, taskName))
                            .collect(Collectors.toSet());
            for (JobKey existingJobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group))) {
                if (!expectedJobKeys.contains(existingJobKey)) {
                    scheduler.deleteJob(existingJobKey);
                }
            }
        } catch (Exception exception) {
            throw operationError("清理一次性 Quartz 孤儿任务失败: " + group, exception);
        }
    }

    private boolean isCurrent(QuartzOneShotTask task) throws Exception {
        var jobKey = jobKey(task);
        var jobDetail = scheduler.getJobDetail(jobKey);
        if (jobDetail == null
                || !jobDetail.isDurable()
                || !jobDetail.getJobClass().equals(task.jobClass())
                || !sameData(jobDetail.getJobDataMap(), task.data())) {
            return false;
        }
        var triggerKey = triggerKey(task);
        var trigger = scheduler.getTrigger(triggerKey);
        if (trigger == null
                || !trigger.getJobKey().equals(jobKey)
                || !(trigger instanceof SimpleTrigger simpleTrigger)
                || simpleTrigger.getRepeatCount() != 0
                || trigger.getMisfireInstruction() != SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW
                || trigger.getNextFireTime() == null
                || trigger.getNextFireTime().getTime() != task.executeAt().toEpochMilli()) {
            return false;
        }
        var triggers = scheduler.getTriggersOfJob(jobKey);
        return triggers.size() == 1 && triggers.getFirst().getKey().equals(triggerKey);
    }

    private boolean sameData(JobDataMap actual, Map<String, ?> expected) {
        return actual.size() == expected.size()
                && expected.entrySet().stream()
                        .allMatch(
                                entry ->
                                        Objects.equals(
                                                actual.get(entry.getKey()), entry.getValue()));
    }

    private Trigger buildTrigger(QuartzOneShotTask task) {
        return TriggerBuilder.newTrigger()
                .withIdentity(triggerKey(task))
                .forJob(jobKey(task))
                .startAt(Date.from(task.executeAt()))
                .withSchedule(
                        SimpleScheduleBuilder.simpleSchedule()
                                .withRepeatCount(0)
                                .withMisfireHandlingInstructionFireNow())
                .build();
    }

    private void removeUnexpectedTriggers(JobKey jobKey, TriggerKey expectedTriggerKey)
            throws Exception {
        for (Trigger existingTrigger : scheduler.getTriggersOfJob(jobKey)) {
            if (!existingTrigger.getKey().equals(expectedTriggerKey)) {
                scheduler.unscheduleJob(existingTrigger.getKey());
            }
        }
    }

    private JobKey jobKey(QuartzOneShotTask task) {
        return jobKey(task.group(), task.taskName());
    }

    private JobKey jobKey(String group, String taskName) {
        return JobKey.jobKey(taskName, group);
    }

    private TriggerKey triggerKey(QuartzOneShotTask task) {
        return TriggerKey.triggerKey(task.taskName() + TRIGGER_SUFFIX, task.group());
    }

    private QuartzOperationException operationError(String message, Exception exception) {
        if (exception instanceof QuartzOperationException operationException) {
            return operationException;
        }
        return new QuartzOperationException(message, exception);
    }

    private void requireGroupOwner(String group, String owner) {
        validateText(group, "Quartz 任务分组不能为空");
        validateText(owner, "Quartz 任务分组所有者不能为空");
        String registeredOwner = groupOwners.get(group);
        if (!owner.equals(registeredOwner)) {
            throw new IllegalStateException("Quartz 任务分组未由当前业务注册: " + group);
        }
    }

    private void requireGroupRegistered(String group) {
        validateText(group, "Quartz 任务分组不能为空");
        if (!groupOwners.containsKey(group)) {
            throw new IllegalStateException("Quartz 任务分组尚未注册: " + group);
        }
    }

    private void validateText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
