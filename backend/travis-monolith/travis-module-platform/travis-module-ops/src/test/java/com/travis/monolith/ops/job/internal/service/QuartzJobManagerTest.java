package com.travis.monolith.ops.job.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.travis.infrastructure.framework.quartz.core.QuartzDispatchJob;
import com.travis.infrastructure.framework.redis.core.annotation.DistributedLock;
import com.travis.infrastructure.framework.redis.core.annotation.DistributedLockNamespace;
import com.travis.monolith.ops.job.api.enums.OpsJobMisfirePolicy;
import com.travis.monolith.ops.job.api.enums.OpsJobStatus;
import com.travis.monolith.ops.job.internal.entity.OpsJob;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

class QuartzJobManagerTest {

    @Test
    void shouldSerializeQuartzMutationsWithReconciliationLock() throws Exception {
        var namespace = QuartzJobManager.class.getAnnotation(DistributedLockNamespace.class);
        assertThat(namespace.value()).isEqualTo(QuartzJobManager.LOCK_NAMESPACE);

        assertSharedLock(QuartzJobManager.class.getMethod("schedule", OpsJob.class));
        assertSharedLock(QuartzJobManager.class.getMethod("delete", Long.class));
        assertSharedLock(QuartzJobManager.class.getMethod("reconcile", List.class));
        assertSharedLock(QuartzJobManager.class.getMethod("runNow", OpsJob.class, String.class));
    }

    @Test
    void shouldKeepDisabledJobDurableWithoutAutomaticTrigger() throws Exception {
        Scheduler scheduler = mock(Scheduler.class);
        var manager = new QuartzJobManager(scheduler);
        OpsJob job = intervalJob(OpsJobStatus.DISABLED.getValue());

        manager.schedule(job);

        var detailCaptor = ArgumentCaptor.forClass(JobDetail.class);
        verify(scheduler).unscheduleJob(TriggerKey.triggerKey("trigger-1001", "ops-job"));
        verify(scheduler).addJob(detailCaptor.capture(), org.mockito.ArgumentMatchers.eq(true));
        verify(scheduler, never()).scheduleJob(any(Trigger.class));
        assertThat(detailCaptor.getValue().isDurable()).isTrue();
    }

    @Test
    void shouldCreateTriggerOnlyWhenJobIsEnabled() throws Exception {
        Scheduler scheduler = mock(Scheduler.class);
        var manager = new QuartzJobManager(scheduler);
        OpsJob job = intervalJob(OpsJobStatus.ENABLED.getValue());
        when(scheduler.rescheduleJob(any(TriggerKey.class), any(Trigger.class))).thenReturn(null);

        manager.schedule(job);

        var triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
        verify(scheduler).scheduleJob(triggerCaptor.capture());
        assertThat(
                        triggerCaptor
                                .getValue()
                                .getJobDataMap()
                                .getString(QuartzDispatchJob.DATA_CONFIG_FINGERPRINT))
                .isNotBlank();
    }

    @Test
    void shouldApplyNextTimeMisfirePolicyToOnceTrigger() throws Exception {
        Scheduler scheduler = mock(Scheduler.class);
        var manager = new QuartzJobManager(scheduler);
        OpsJob job = onceJob();
        when(scheduler.rescheduleJob(any(TriggerKey.class), any(Trigger.class))).thenReturn(null);

        manager.schedule(job);

        var triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
        verify(scheduler).scheduleJob(triggerCaptor.capture());
        var trigger = (SimpleTrigger) triggerCaptor.getValue();
        assertThat(trigger.getRepeatCount()).isZero();
        assertThat(trigger.getMisfireInstruction())
                .isEqualTo(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT);
    }

    @Test
    void shouldRecognizeMatchingPersistedConfiguration() throws Exception {
        Scheduler scheduler = mock(Scheduler.class);
        var manager = new QuartzJobManager(scheduler);
        OpsJob job = intervalJob(OpsJobStatus.ENABLED.getValue());
        when(scheduler.rescheduleJob(any(TriggerKey.class), any(Trigger.class))).thenReturn(null);
        var detailCaptor = ArgumentCaptor.forClass(JobDetail.class);
        var triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
        manager.schedule(job);
        verify(scheduler).addJob(detailCaptor.capture(), org.mockito.ArgumentMatchers.eq(true));
        verify(scheduler).scheduleJob(triggerCaptor.capture());
        when(scheduler.getJobDetail(detailCaptor.getValue().getKey()))
                .thenReturn(detailCaptor.getValue());
        when(scheduler.getTrigger(triggerCaptor.getValue().getKey()))
                .thenReturn(triggerCaptor.getValue());

        assertThat(manager.isSynchronized(job)).isTrue();
    }

    private OpsJob intervalJob(Integer status) {
        var job = baseJob(status);
        job.setScheduleType("INTERVAL");
        job.setIntervalMillis(60_000L);
        return job;
    }

    private void assertSharedLock(java.lang.reflect.Method method) {
        var lock = method.getAnnotation(DistributedLock.class);
        assertThat(lock).isNotNull();
        assertThat(lock.key()).isEqualTo("'" + QuartzJobManager.RECONCILE_LOCK_KEY + "'");
        assertThat(lock.waitTime()).isEqualTo(30);
        assertThat(lock.timeUnit()).isEqualTo(TimeUnit.SECONDS);
    }

    private OpsJob onceJob() {
        var job = baseJob(OpsJobStatus.ENABLED.getValue());
        job.setScheduleType("ONCE");
        job.setExecuteAt(LocalDateTime.now().plusHours(1));
        job.setMisfirePolicy(OpsJobMisfirePolicy.NEXT_TIME.getValue());
        return job;
    }

    private OpsJob baseJob(Integer status) {
        var job = new OpsJob();
        job.setId(1001L);
        job.setJobName("测试任务");
        job.setHandlerName("testHandler");
        job.setParams("{}");
        job.setPriority(5);
        job.setConcurrent(0);
        job.setMisfirePolicy(OpsJobMisfirePolicy.SMART.getValue());
        job.setStatus(status);
        return job;
    }
}
