package com.travis.infrastructure.framework.web.core.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 业务异常 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public final class BizException extends RuntimeException {

    public BizException(IErrorCode errorCode) {
        this.errorCode = errorCode;
        this.args = null;
    }

    /** 错误码 */
    private final IErrorCode errorCode;

    /** 错误参数 */
    private final Object[] args;
}
