package com.travis.monolith.ops.job.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 任务并发策略枚举。 */
@Getter
@AllArgsConstructor
public enum OpsJobConcurrentPolicy {
    FORBID(0),
    ALLOW(1);

    private final Integer value;
}
