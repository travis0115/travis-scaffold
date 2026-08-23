package com.travis.monolith.ops.job.api.enums;

import java.time.LocalDate;

/** 任务调度看板执行指标时间范围。 */
public enum OpsJobDashboardRange {
    TODAY(1),
    LAST_7_DAYS(7),
    LAST_30_DAYS(30);

    private final int days;

    OpsJobDashboardRange(int days) {
        this.days = days;
    }

    /** 根据结束日期计算包含当天的范围起始日期。 */
    public LocalDate startDate(LocalDate today) {
        return today.minusDays(days - 1L);
    }
}
