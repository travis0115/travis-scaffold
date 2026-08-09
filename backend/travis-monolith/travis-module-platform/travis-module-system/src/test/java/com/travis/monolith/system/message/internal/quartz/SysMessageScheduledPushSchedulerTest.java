package com.travis.monolith.system.message.internal.quartz;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.travis.infrastructure.framework.quartz.core.QuartzOneShotManager;
import com.travis.infrastructure.framework.quartz.core.QuartzOneShotTask;
import com.travis.monolith.system.message.internal.entity.SysMessage;
import com.travis.monolith.system.message.internal.mapper.SysMessageMapper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SysMessageScheduledPushSchedulerTest {

    @BeforeAll
    static void initializeTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""), SysMessage.class);
    }

    private final QuartzOneShotManager oneShotManager = mock(QuartzOneShotManager.class);
    private final SysMessageMapper messageMapper = mock(SysMessageMapper.class);
    private final SysMessageScheduledPushScheduler scheduler =
            new SysMessageScheduledPushScheduler(oneShotManager, messageMapper);

    @Test
    void shouldCreateExpectedOneShotTaskForScheduledMessage() {
        var publishTime = LocalDateTime.of(2026, 8, 9, 18, 0);
        var message = new SysMessage();
        message.setId(100L);
        message.setPublishTime(publishTime);
        when(messageMapper.selectOne(any())).thenReturn(message);

        scheduler.sync(100L);

        var taskCaptor = ArgumentCaptor.forClass(QuartzOneShotTask.class);
        verify(oneShotManager).sync(taskCaptor.capture());
        var task = taskCaptor.getValue();
        assertThat(task.group()).isEqualTo("system-message");
        assertThat(task.taskName()).isEqualTo("scheduled-message-push-100");
        assertThat(task.jobClass()).isEqualTo(SysMessageScheduledPushJob.class);
        assertThat(task.data().get(SysMessageScheduledPushJob.DATA_MESSAGE_ID)).isEqualTo(100L);
        assertThat(task.executeAt())
                .isEqualTo(publishTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    @Test
    void shouldDeleteTaskWhenMessageNoLongerNeedsScheduling() {
        when(messageMapper.selectOne(any())).thenReturn(null);

        scheduler.sync(100L);

        verify(oneShotManager).delete("system-message", "scheduled-message-push-100");
    }
}
