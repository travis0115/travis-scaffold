package com.travis.monolith.ops.job.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 任务执行状态枚举。 */
@Getter
@AllArgsConstructor
public enum OpsJobLogStatus {
    RUNNING(0),
    SUCCESS(1),
    FAILED(2);

    private final Integer value;
}
