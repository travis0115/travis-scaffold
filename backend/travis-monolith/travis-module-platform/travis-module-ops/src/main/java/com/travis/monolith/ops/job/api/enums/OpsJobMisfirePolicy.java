package com.travis.monolith.ops.job.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 任务错过执行策略枚举。 */
@Getter
@AllArgsConstructor
public enum OpsJobMisfirePolicy {
    SMART(0),
    IGNORE(1),
    FIRE_NOW(2),
    NEXT_TIME(3);

    private final Integer value;
}
