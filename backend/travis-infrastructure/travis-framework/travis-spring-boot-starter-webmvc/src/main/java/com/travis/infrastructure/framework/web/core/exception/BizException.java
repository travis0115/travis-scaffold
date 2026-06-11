package com.travis.infrastructure.framework.web.core.exception;

import com.travis.infrastructure.common.web.exception.ErrorCode;
import com.travis.infrastructure.common.web.exception.ErrorCodeException;

/** 业务异常 */
public final class BizException extends ErrorCodeException {

    public BizException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BizException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
}
