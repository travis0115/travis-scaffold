package com.travis.monolith.ops.job.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.travis.monolith.ops.job.internal.config.OpsJobProperties;
import com.travis.monolith.ops.job.internal.converter.OpsJobLogConverter;
import com.travis.monolith.ops.job.internal.entity.OpsJob;
import com.travis.monolith.ops.job.internal.mapper.OpsJobLogMapper;
import com.travis.monolith.ops.job.internal.mapper.OpsJobMapper;
import com.travis.monolith.ops.job.internal.model.OpsJobLogStatsSummary;
import com.travis.monolith.ops.job.internal.model.OpsJobLogTrendPoint;
import com.travis.monolith.ops.job.internal.service.impl.OpsJobLogServiceImpl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class OpsJobLogServiceImplTest {

    @Test
    void shouldRejectStatsForMissingJob() {
        OpsJobLogMapper mapper = mock(OpsJobLogMapper.class);
        var service =
                new OpsJobLogServiceImpl(
                        mock(OpsJobLogConverter.class),
                        new OpsJobProperties(),
                        mock(OpsJobMapper.class));
        ReflectionTestUtils.setField(service, "baseMapper", mapper);

        assertThatThrownBy(() -> service.stats(1001L)).hasMessageContaining("调度任务不存在");
    }

    @Test
    void shouldCalculateStatsFromDatabaseAggregatesAndIgnoreRunningInSuccessRate() {
        OpsJobLogMapper mapper = mock(OpsJobLogMapper.class);
        OpsJobMapper jobMapper = mock(OpsJobMapper.class);
        var service =
                new OpsJobLogServiceImpl(
                        mock(OpsJobLogConverter.class), new OpsJobProperties(), jobMapper);
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        when(jobMapper.selectById(1001L)).thenReturn(new OpsJob());
        when(mapper.selectStatsSummary(1001L))
                .thenReturn(new OpsJobLogStatsSummary(4, 2, 1, 120, 300));
        when(mapper.selectP95Duration(1001L)).thenReturn(280L);
        when(mapper.selectConsecutiveFailures(1001L)).thenReturn(1L);
        LocalDate today = LocalDate.now();
        when(mapper.selectTrend(any(), any(LocalDateTime.class)))
                .thenReturn(List.of(new OpsJobLogTrendPoint(today, 2, 1)));

        var result = service.stats(1001L);

        assertThat(result.total()).isEqualTo(4);
        assertThat(result.successRate()).isEqualTo(200.0 / 3);
        assertThat(result.p95DurationMillis()).isEqualTo(280);
        assertThat(result.consecutiveFailures()).isEqualTo(1);
        assertThat(result.trend()).hasSize(7);
        assertThat(result.trend().getLast())
                .isEqualTo(
                        new com.travis.monolith.ops.job.api.response.OpsJobStatsResp.TrendPoint(
                                today, 2, 1));
    }
}
