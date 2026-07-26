package com.travis.monolith.system.message.internal.quartz;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.travis.monolith.system.message.internal.service.SysMessageService;
import org.junit.jupiter.api.Test;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

class SysMessageScheduledPushJobTest {

    @Test
    void shouldPushOnlyTheMessageStoredInJobData() throws Exception {
        SysMessageService messageService = mock(SysMessageService.class);
        JobExecutionContext context = mock(JobExecutionContext.class);
        var data = new JobDataMap();
        data.put(SysMessageScheduledPushJob.DATA_MESSAGE_ID, 1001L);
        when(context.getMergedJobDataMap()).thenReturn(data);
        when(messageService.pushScheduled(1001L)).thenReturn(true);

        new SysMessageScheduledPushJob(messageService).execute(context);

        verify(messageService).pushScheduled(1001L);
    }
}
