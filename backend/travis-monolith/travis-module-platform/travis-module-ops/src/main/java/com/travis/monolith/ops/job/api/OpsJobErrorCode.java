package com.travis.monolith.ops.job.api;

import com.travis.infrastructure.common.web.exception.ErrorCode;
import lombok.AllArgsConstructor;

/** 任务调度模块错误码。 */
@AllArgsConstructor
public enum OpsJobErrorCode implements ErrorCode {
    JOB_NOT_FOUND("OPS_JOB_001", "调度任务不存在"),
    HANDLER_NOT_FOUND("OPS_JOB_002", "任务处理器未注册：{0}"),
    INVALID_SCHEDULE("OPS_JOB_003", "调度配置不正确：{0}"),
    INVALID_PARAMS("OPS_JOB_004", "任务参数不正确：{0}"),
    SCHEDULER_ERROR("OPS_JOB_005", "Quartz 调度操作失败：{0}"),
    LOG_NOT_FOUND("OPS_JOB_006", "任务执行日志不存在"),
    USER_OUT_OF_SCOPE("OPS_JOB_007", "告警接收人不在当前用户可选范围内"),
    CONCURRENT_UPDATE("OPS_JOB_008", "调度任务已被其他请求修改，请刷新后重试"),
    BUILTIN_NOT_MODIFIABLE("OPS_JOB_009", "系统内置调度任务不允许修改、启停、删除或复制"),
    BUILTIN_HANDLER_RESERVED("OPS_JOB_010", "系统内置任务处理器不允许用于自定义任务：{0}");

    /** 错误码。 */
    private final String code;

    /** 错误消息模板。 */
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
