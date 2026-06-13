package com.travis.monolith.ops.job.internal.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record OpsJobCalendarConfig(
        List<LocalDate> excludedDates,
        List<Integer> excludedWeekdays,
        LocalTime dailyStartTime,
        LocalTime dailyEndTime) {

    public OpsJobCalendarConfig {
        excludedDates = excludedDates == null ? List.of() : List.copyOf(excludedDates);
        excludedWeekdays = excludedWeekdays == null ? List.of() : List.copyOf(excludedWeekdays);
    }

    public boolean isEmpty() {
        return excludedDates.isEmpty()
                && excludedWeekdays.isEmpty()
                && dailyStartTime == null
                && dailyEndTime == null;
    }
}
