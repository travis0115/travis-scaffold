package com.travis.monolith.ops.errorlog.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 错误日志处理状态。 */
@Getter
@AllArgsConstructor
public enum SysErrorLogHandleStatus {
    PENDING(0),
    RESOLVED(1),
    IGNORED(2);

    private final Integer value;
}
