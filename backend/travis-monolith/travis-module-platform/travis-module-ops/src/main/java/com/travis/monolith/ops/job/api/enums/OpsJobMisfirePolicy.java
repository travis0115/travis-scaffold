package com.travis.monolith.ops.job.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 任务错过执行策略枚举。 */
@Getter
@AllArgsConstructor
public enum OpsJobMisfirePolicy {
    /** 智能策略：根据触发器类型自动选择。 */
    SMART(0),
    /** 忽略：按原计划追赶被错过的执行。 */
    IGNORE(1),
    /** 立即执行：立即补执行一次。 */
    FIRE_NOW(2),
    /** 下次执行：等到下次计划时间再执行。 */
    NEXT_TIME(3);

    /** 策略值。 */
    private final Integer value;
}
