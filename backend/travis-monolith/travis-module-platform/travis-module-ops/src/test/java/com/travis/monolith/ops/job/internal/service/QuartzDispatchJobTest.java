package com.travis.monolith.ops.job.internal.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.travis.infrastructure.framework.quartz.core.QuartzDispatchJob;
import com.travis.infrastructure.framework.quartz.core.QuartzJobExecutionObserver;
import com.travis.infrastructure.framework.quartz.core.QuartzJobHandler;
import com.travis.infrastructure.framework.quartz.core.QuartzJobHandlerRegistry;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

class QuartzDispatchJobTest {

    @Test
    void shouldExecuteHandlerWhenBeforeObserverFails() throws Exception {
        QuartzJobHandler handler = mock(QuartzJobHandler.class);
        QuartzJobExecutionObserver observer = mock(QuartzJobExecutionObserver.class);
        JobExecutionContext context = context();
        when(handler.getName()).thenReturn("testHandler");
        doThrow(new IllegalStateException("日志不可用")).when(observer).beforeExecution(context);
        var job = new QuartzDispatchJob(new QuartzJobHandlerRegistry(List.of(handler)), observer);

        assertThatCode(() -> job.execute(context)).doesNotThrowAnyException();

        verify(handler).execute("{}");
        verify(observer)
                .afterSuccess(
                        org.mockito.ArgumentMatchers.eq(context),
                        org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void shouldNotFailBusinessExecutionWhenSuccessObserverFails() throws Exception {
        QuartzJobHandler handler = mock(QuartzJobHandler.class);
        QuartzJobExecutionObserver observer = mock(QuartzJobExecutionObserver.class);
        JobExecutionContext context = context();
        when(handler.getName()).thenReturn("testHandler");
        doThrow(new IllegalStateException("日志不可用"))
                .when(observer)
                .afterSuccess(
                        org.mockito.ArgumentMatchers.eq(context),
                        org.mockito.ArgumentMatchers.anyLong());
        var job = new QuartzDispatchJob(new QuartzJobHandlerRegistry(List.of(handler)), observer);

        assertThatCode(() -> job.execute(context)).doesNotThrowAnyException();

        verify(handler).execute("{}");
    }

    private JobExecutionContext context() {
        JobExecutionContext context = mock(JobExecutionContext.class);
        var data = new JobDataMap();
        data.put(QuartzDispatchJob.DATA_HANDLER_NAME, "testHandler");
        data.put(QuartzDispatchJob.DATA_PARAMS, "{}");
        when(context.getMergedJobDataMap()).thenReturn(data);
        when(context.getFireInstanceId()).thenReturn("fire-1");
        return context;
    }
}
