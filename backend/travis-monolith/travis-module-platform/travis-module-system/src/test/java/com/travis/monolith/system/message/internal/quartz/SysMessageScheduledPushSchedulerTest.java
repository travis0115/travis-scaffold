package com.travis.monolith.system.message.internal.quartz;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.travis.monolith.system.message.internal.mapper.SysMessageMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

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
}
