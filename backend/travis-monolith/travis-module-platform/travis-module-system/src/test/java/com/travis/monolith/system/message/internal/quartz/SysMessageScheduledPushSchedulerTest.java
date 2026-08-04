package com.travis.monolith.system.message.internal.quartz;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.travis.monolith.system.message.internal.mapper.SysMessageMapper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

class SysMessageScheduledPushSchedulerTest {

    @Test
    void shouldCreateOneShotTriggerThatFiresAfterMisfire() throws Exception {
        Scheduler quartzScheduler = mock(Scheduler.class);
        SysMessageMapper messageMapper = mock(SysMessageMapper.class);
        var scheduler = new SysMessageScheduledPushScheduler(quartzScheduler, messageMapper);
        long messageId = 1001L;
        LocalDateTime publishTime = LocalDateTime.of(2026, 7, 26, 12, 30);
        when(quartzScheduler.checkExists(any(org.quartz.JobKey.class))).thenReturn(false);

        scheduler.schedule(messageId, publishTime);

        var jobCaptor = ArgumentCaptor.forClass(JobDetail.class);
        var triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
        verify(quartzScheduler).scheduleJob(jobCaptor.capture(), triggerCaptor.capture());
        JobDetail job = jobCaptor.getValue();
        SimpleTrigger trigger = (SimpleTrigger) triggerCaptor.getValue();
        assertThat(job.getKey().getName()).isEqualTo("scheduled-message-push-" + messageId);
        assertThat(job.getJobDataMap().getLong(SysMessageScheduledPushJob.DATA_MESSAGE_ID))
                .isEqualTo(messageId);
        assertThat(trigger.getRepeatCount()).isZero();
        assertThat(trigger.getMisfireInstruction())
                .isEqualTo(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
        assertThat(trigger.getJobKey()).isEqualTo(job.getKey());
    }

    @Test
    void shouldRescheduleExistingMessageTrigger() throws Exception {
        Scheduler quartzScheduler = mock(Scheduler.class);
        SysMessageMapper messageMapper = mock(SysMessageMapper.class);
        var scheduler = new SysMessageScheduledPushScheduler(quartzScheduler, messageMapper);
        when(quartzScheduler.checkExists(any(org.quartz.JobKey.class))).thenReturn(true);
        when(quartzScheduler.rescheduleJob(any(), any())).thenReturn(new java.util.Date());

        scheduler.schedule(1001L, LocalDateTime.of(2026, 7, 26, 12, 30));

        verify(quartzScheduler).rescheduleJob(any(), any());
    }

    @Test
    void shouldRecreateMissingTriggerDuringReconciliation() throws Exception {
        Scheduler quartzScheduler = mock(Scheduler.class);
        SysMessageMapper messageMapper = mock(SysMessageMapper.class);
        var scheduler = new SysMessageScheduledPushScheduler(quartzScheduler, messageMapper);
        var message = new com.travis.monolith.system.message.internal.entity.SysMessage();
        message.setId(1001L);
        message.setPublishTime(LocalDateTime.of(2026, 7, 26, 12, 30));
        when(messageMapper.selectList(any())).thenReturn(List.of(message));
        when(quartzScheduler.checkExists(any(TriggerKey.class))).thenReturn(false);
        when(quartzScheduler.checkExists(any(org.quartz.JobKey.class))).thenReturn(false);

        scheduler.reconcile();

        verify(quartzScheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    void shouldRescheduleTriggerWithOutdatedFireTimeDuringReconciliation() throws Exception {
        Scheduler quartzScheduler = mock(Scheduler.class);
        SysMessageMapper messageMapper = mock(SysMessageMapper.class);
        var scheduler = new SysMessageScheduledPushScheduler(quartzScheduler, messageMapper);
        LocalDateTime publishTime = LocalDateTime.of(2026, 7, 26, 12, 30);
        var message = new com.travis.monolith.system.message.internal.entity.SysMessage();
        message.setId(1001L);
        message.setPublishTime(publishTime);
        Trigger existingTrigger = mock(Trigger.class);
        when(existingTrigger.getNextFireTime())
                .thenReturn(
                        Date.from(
                                publishTime
                                        .plusHours(1)
                                        .atZone(ZoneId.systemDefault())
                                        .toInstant()));
        when(messageMapper.selectList(any())).thenReturn(List.of(message));
        when(quartzScheduler.getTrigger(any())).thenReturn(existingTrigger);
        when(quartzScheduler.checkExists(any(org.quartz.JobKey.class))).thenReturn(true);
        when(quartzScheduler.rescheduleJob(any(), any())).thenReturn(new Date());

        scheduler.reconcile();

        verify(quartzScheduler).rescheduleJob(any(), any());
    }

    @Test
    void shouldKeepTriggerWithCurrentFireTimeDuringReconciliation() throws Exception {
        Scheduler quartzScheduler = mock(Scheduler.class);
        SysMessageMapper messageMapper = mock(SysMessageMapper.class);
        var scheduler = new SysMessageScheduledPushScheduler(quartzScheduler, messageMapper);
        LocalDateTime publishTime = LocalDateTime.of(2026, 7, 26, 12, 30);
        var message = new com.travis.monolith.system.message.internal.entity.SysMessage();
        message.setId(1001L);
        message.setPublishTime(publishTime);
        Trigger existingTrigger = mock(Trigger.class);
        when(existingTrigger.getNextFireTime())
                .thenReturn(Date.from(publishTime.atZone(ZoneId.systemDefault()).toInstant()));
        when(messageMapper.selectList(any())).thenReturn(List.of(message));
        when(quartzScheduler.getTrigger(any())).thenReturn(existingTrigger);

        scheduler.reconcile();

        verify(quartzScheduler, never()).rescheduleJob(any(), any());
        verify(quartzScheduler, never()).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    void shouldDeleteOrphanJobDuringReconciliation() throws Exception {
        Scheduler quartzScheduler = mock(Scheduler.class);
        SysMessageMapper messageMapper = mock(SysMessageMapper.class);
        var scheduler = new SysMessageScheduledPushScheduler(quartzScheduler, messageMapper);
        JobKey orphanJob = JobKey.jobKey("scheduled-message-push-1001", "system-message");
        when(messageMapper.selectList(any())).thenReturn(List.of());
        when(quartzScheduler.getJobKeys(any())).thenReturn(Set.of(orphanJob));

        scheduler.reconcile();

        verify(quartzScheduler).deleteJob(orphanJob);
    }
}
