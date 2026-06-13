package com.travis.monolith.ops.job.api;

import com.travis.infrastructure.common.web.exception.ErrorCode;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum OpsJobErrorCode implements ErrorCode {
    JOB_NOT_FOUND("OPS_JOB_001", "调度任务不存在"),
    HANDLER_NOT_FOUND("OPS_JOB_002", "任务处理器未注册：{0}"),
    INVALID_SCHEDULE("OPS_JOB_003", "调度配置不正确：{0}"),
    INVALID_PARAMS("OPS_JOB_004", "任务参数不正确：{0}"),
    SCHEDULER_ERROR("OPS_JOB_005", "Quartz 调度操作失败：{0}"),
    LOG_NOT_FOUND("OPS_JOB_006", "任务执行日志不存在"),
    USER_OUT_OF_SCOPE("OPS_JOB_007", "负责人或告警接收人不在当前用户可选范围内");

    private final String code;
    private final String msg;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }
}
