package com.travis.monolith.ops.job.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.travis.monolith.ops.job.api.enums.OpsJobDashboardRange;
import com.travis.monolith.ops.job.internal.mapper.OpsJobLogMapper;
import com.travis.monolith.ops.job.internal.mapper.OpsJobMapper;
import com.travis.monolith.ops.job.internal.model.OpsJobCountSummary;
import com.travis.monolith.ops.job.internal.model.OpsJobLogStatsSummary;
import com.travis.monolith.ops.job.internal.model.OpsJobLogTrendPoint;
import com.travis.monolith.ops.job.internal.service.impl.OpsJobDashboardServiceImpl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class OpsJobDashboardServiceImplTest {

    @Test
    void shouldSeparateCurrentJobCountsFromRangedExecutionMetrics() {
        var jobMapper = mock(OpsJobMapper.class);
        var logMapper = mock(OpsJobLogMapper.class);
        when(jobMapper.selectCountSummary()).thenReturn(new OpsJobCountSummary(10, 7));
        when(logMapper.selectDashboardStatsSummary(
                        any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new OpsJobLogStatsSummary(8, 5, 2, 100, 300));
        when(logMapper.selectDashboardTrend(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(new OpsJobLogTrendPoint(LocalDate.now(), 5, 2)));
        var service = new OpsJobDashboardServiceImpl(jobMapper, logMapper);

        var result = service.dashboard(OpsJobDashboardRange.TODAY);

        assertThat(result.totalJobs()).isEqualTo(10);
        assertThat(result.enabledJobs()).isEqualTo(7);
        assertThat(result.pausedJobs()).isEqualTo(3);
        assertThat(result.executions()).isEqualTo(8);
        assertThat(result.runningExecutions()).isEqualTo(1);
        assertThat(result.successRate()).isEqualTo(5 * 100.0 / 7);
        assertThat(result.trend()).hasSize(7);
        assertThat(result.trend().getLast().success()).isEqualTo(5);
    }
}
