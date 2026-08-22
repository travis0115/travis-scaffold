package com.travis.monolith.ops.job.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.annotation.Version;
import com.travis.infrastructure.framework.quartz.core.QuartzJobHandlerRegistry;
import com.travis.monolith.ops.job.api.request.OpsJobUpdateReq;
import com.travis.monolith.ops.job.internal.converter.OpsJobConverter;
import com.travis.monolith.ops.job.internal.entity.OpsJob;
import com.travis.monolith.ops.job.internal.mapper.OpsJobMapper;
import com.travis.monolith.ops.job.internal.service.impl.OpsJobServiceImpl;
import com.travis.monolith.system.user.api.SysUserApi;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class OpsJobOptimisticLockTest {

    @Test
    void shouldDeclareAndRequireLockVersion() throws Exception {
        assertThat(OpsJob.class.getDeclaredField("lockVersion").getAnnotation(Version.class))
                .isNotNull();
        assertThat(
                        OpsJobUpdateReq.class
                                .getDeclaredField("lockVersion")
                                .getAnnotation(NotNull.class))
                .isNotNull();
    }

    @Test
    void shouldRejectStaleJobUpdate() {
        var mapper = mock(OpsJobMapper.class);
        var converter = mock(OpsJobConverter.class);
        var job = new OpsJob();
        job.setId(1L);
        job.setLockVersion(2);
        job.setStatus(0);
        job.setScheduleType("INTERVAL");
        job.setIntervalMillis(1000L);
        when(mapper.selectById(1L)).thenReturn(job);
        when(mapper.updateById(job)).thenReturn(0);
        var service =
                new OpsJobServiceImpl(
                        mock(QuartzJobManager.class),
                        mock(QuartzJobHandlerRegistry.class),
                        mock(SysUserApi.class),
                        converter,
                        mock(OpsJobLogService.class),
                        mock(OpsJobDashboardService.class));
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        var request = new OpsJobUpdateReq();
        request.setLockVersion(1);

        assertThatThrownBy(() -> service.update(1L, request)).hasMessageContaining("已被其他请求修改");
    }
}
