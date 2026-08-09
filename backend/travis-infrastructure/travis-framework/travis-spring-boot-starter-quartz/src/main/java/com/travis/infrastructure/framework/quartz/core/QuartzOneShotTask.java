package com.travis.infrastructure.framework.quartz.core;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import org.quartz.Job;

/** 一次性 Quartz 任务的期望状态。 */
public record QuartzOneShotTask(
        String group,
        String taskName,
        Class<? extends Job> jobClass,
        Map<String, ?> data,
        Instant executeAt,
        String description) {

    public QuartzOneShotTask {
        if (group == null || group.isBlank()) {
            throw new IllegalArgumentException("Quartz 任务分组不能为空");
        }
        if (taskName == null || taskName.isBlank()) {
            throw new IllegalArgumentException("Quartz 任务名称不能为空");
        }
        Objects.requireNonNull(jobClass, "Quartz Job 类型不能为空");
        Objects.requireNonNull(executeAt, "Quartz 执行时间不能为空");
        data = data == null ? Map.of() : Map.copyOf(data);
    }
}
