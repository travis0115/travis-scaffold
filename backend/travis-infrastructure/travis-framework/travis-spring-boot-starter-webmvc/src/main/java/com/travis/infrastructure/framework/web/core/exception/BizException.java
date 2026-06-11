package com.travis.infrastructure.framework.web.core.exception;

import com.travis.infrastructure.common.web.exception.ErrorCode;

/** 业务异常 */
public final class BizException extends com.travis.infrastructure.common.web.exception.BizException {

    public BizException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BizException(ErrorCode errorCode, Object... args) {
        super(errorCode, args);
    }
}
