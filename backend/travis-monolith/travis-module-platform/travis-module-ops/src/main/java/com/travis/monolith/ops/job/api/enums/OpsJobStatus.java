package com.travis.monolith.ops.job.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 任务状态枚举。 */
@Getter
@AllArgsConstructor
public enum OpsJobStatus {
    DISABLED(0),
    ENABLED(1);

    private final Integer value;
}
