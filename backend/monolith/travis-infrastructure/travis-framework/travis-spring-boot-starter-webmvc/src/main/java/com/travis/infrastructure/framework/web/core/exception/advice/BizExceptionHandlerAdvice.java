package com.travis.infrastructure.framework.web.core.exception.advice;

import com.travis.infrastructure.framework.web.core.exception.BizException;
import com.travis.infrastructure.framework.web.core.exception.CommonErrorCode;
import com.travis.infrastructure.framework.web.core.model.ApiResponse;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 业务异常处理器
 *
 * @author travis
 */
@RestControllerAdvice
@Order
@Slf4j
@NoArgsConstructor
public class BizExceptionHandlerAdvice {

    /** 业务异常 */
    @ExceptionHandler(BizException.class)
    public ApiResponse<?> handleBizException(BizException ex) {
        log.warn("业务异常: {}", ex.getErrorCode().getMsg(), ex);
        return ApiResponse.error(ex.getErrorCode(), ex.getArgs());
    }

    /** 兜底 */
    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(Exception ex) {
        log.error("系统异常: ", ex);
        return ApiResponse.error(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
}
