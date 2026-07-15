package com.travis.monolith.ops.job.internal.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** 定时任务的日期与每日执行时段限制。 */
public record OpsJobCalendarConfig(
        /** 不执行任务的日期列表。 */
        List<LocalDate> excludedDates,
        /** 不执行任务的星期列表。 */
        List<Integer> excludedWeekdays,
        /** 每日允许执行时间段的起点。 */
        LocalTime dailyStartTime,
        /** 每日允许执行时间段的终点。 */
        LocalTime dailyEndTime) {

    public OpsJobCalendarConfig {
        excludedDates = excludedDates == null ? List.of() : List.copyOf(excludedDates);
        excludedWeekdays = excludedWeekdays == null ? List.of() : List.copyOf(excludedWeekdays);
    }

    /** 判断是否未配置任何日期或时段限制。 */
    public boolean isEmpty() {
        return excludedDates.isEmpty()
                && excludedWeekdays.isEmpty()
                && dailyStartTime == null
                && dailyEndTime == null;
    }
}
