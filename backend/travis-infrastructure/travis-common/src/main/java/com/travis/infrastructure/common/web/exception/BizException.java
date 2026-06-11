package com.travis.infrastructure.common.web.exception;

import lombok.Getter;

/** 携带统一错误码的运行时异常，供基础设施组件抛出可识别的业务错误。 */
@Getter
public class BizException extends RuntimeException {

    private final ErrorCode errorCode;

    private final Object[] args;

    public BizException(ErrorCode errorCode, Object... args) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
        this.args = args;
    }

    public BizException(ErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode.getMsg(), cause);
        this.errorCode = errorCode;
        this.args = args;
    }
}
