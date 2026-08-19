package com.travis.monolith.ops.errorlog.api;

import com.travis.infrastructure.common.web.exception.ErrorCode;
import lombok.AllArgsConstructor;

/** 错误日志模块错误码。 */
@AllArgsConstructor
public enum OpsErrorLogErrorCode implements ErrorCode {
    LOG_NOT_FOUND("OPS_ERROR_LOG_001", "错误日志不存在"),
    LOG_ALREADY_HANDLED("OPS_ERROR_LOG_002", "错误日志已处理，请勿重复操作");

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
