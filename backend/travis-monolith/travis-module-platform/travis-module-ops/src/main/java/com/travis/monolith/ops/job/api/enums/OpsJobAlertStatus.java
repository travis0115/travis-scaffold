package com.travis.monolith.ops.job.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 任务执行失败告警状态。 */
@Getter
@AllArgsConstructor
public enum OpsJobAlertStatus {
    NOT_SENT(0),
    SENT(1);

    /** 状态值。 */
    private final Integer value;
}
