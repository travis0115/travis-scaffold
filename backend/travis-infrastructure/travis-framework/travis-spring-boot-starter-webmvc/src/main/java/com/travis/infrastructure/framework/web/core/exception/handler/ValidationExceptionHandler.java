package com.travis.infrastructure.framework.web.core.exception.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.travis.infrastructure.common.web.constant.ExceptionHandlerOrder;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.util.stream.Collectors;

/**
 * 参数校验异常处理器
 *
 * @author Travis
 */
@RestControllerAdvice
@Order(ExceptionHandlerOrder.VALIDATION_EXCEPTION_HANDLER)
@Slf4j
@NoArgsConstructor
public class ValidationExceptionHandler {

    /** 数字格式错误 */
    @ExceptionHandler(NumberFormatException.class)
    public ApiResponse<?> handleNumberFormatException(NumberFormatException ex) {
        log.warn("数字格式错误：", ex);
        return ApiResponse.error(CommonErrorCode.VALIDATE_NUMBERFORMAT_EXCEPTION);
    }

    /** Validator 校验不通过 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<?> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("参数校验失败：", ex);
        var constraintViolations = ex.getConstraintViolations();
        var errorMessage =
                CollUtil.isEmpty(constraintViolations)
                        ? ""
                        : constraintViolations.iterator().next().getMessage();
        return ApiResponse.error(CommonErrorCode.VALIDATE_FAILED, errorMessage);
    }

    /** 参数绑定异常 */
    @ExceptionHandler(BindException.class)
    public ApiResponse<?> handleBindException(BindException ex) {
        log.warn("参数绑定异常：", ex);
        var errorMsg =
                ex.getFieldErrors().stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.joining(", "));
        return ApiResponse.error(CommonErrorCode.VALIDATE_FAILED, errorMsg);
    }

    /**
     * SpringMVC 请求参数缺失
     *
     * <p>例如说，接口上设置了 @RequestParam("xx") 参数，结果并未传递 xx 参数
     */
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public ApiResponse<?> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex) {
        log.warn("请求参数缺失：", ex);
        return ApiResponse.error(
                CommonErrorCode.VALIDATE_MISSING_SERVLET_REQUEST_PARAMETER, ex.getParameterName());
    }

    /**
     * SpringMVC 路径变量缺失
     *
     * <p>例如说，接口上设置了 @PathVariable("id") 参数，结果请求路径中未包含该变量
     */
    @ExceptionHandler(MissingPathVariableException.class)
    public ApiResponse<?> handleMissingPathVariableException(MissingPathVariableException ex) {
        log.warn("请求路径变量缺失：", ex);
        return ApiResponse.error(
                CommonErrorCode.VALIDATE_MISSING_PATH_VARIABLE, ex.getVariableName());
    }

    /**
     * SpringMVC 请求参数类型错误
     *
     * <p>例如说，接口上设置了 @RequestParam("xx") 参数为 Integer，结果传递 xx 参数类型为 String
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResponse<?> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        log.warn("请求参数类型错误：", ex);
        return ApiResponse.error(
                CommonErrorCode.VALIDATE_METHOD_ARGUMENT_TYPE_MISMATCH, ex.getName());
    }

    /**
     * SpringMVC 请求参数类型错误
     *
     * <p>例如说，接口上设置了 @RequestBody 实体中 xx 属性类型为 Integer，结果传递 xx 参数类型为 String
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<?> handlerMethodArgumentTypeInvalidFormatException(
            HttpMessageNotReadableException ex) {
        log.warn("请求参数类型错误：", ex);
        if (ex.getCause() instanceof InvalidFormatException invalidFormatException) {
            return ApiResponse.error(
                    CommonErrorCode.VALIDATE_METHOD_ARGUMENT_TYPE_MISMATCH,
                    invalidFormatException.getValue());
        }
        if (StrUtil.startWith(ex.getMessage(), "Required request body is missing")) {
            return ApiResponse.error(CommonErrorCode.VALIDATE_MISSING_REQUIRED_REQUEST_BODY);
        }
        return ApiResponse.error(CommonErrorCode.VALIDATE_FAILED, ex.getMessage());
    }

    /** SpringMVC 参数校验失败 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleMethodArgumentNotValidExceptionException(
            MethodArgumentNotValidException ex) {
        log.warn("参数校验失败：", ex);
        // 获取 errorMessage
        String errorMessage = null;
        var fieldError = ex.getBindingResult().getFieldError();
        if (fieldError == null) {
            // 组合校验，参考自 https://t.zsxq.com/3HVTx
            var allErrors = ex.getBindingResult().getAllErrors();
            if (CollUtil.isNotEmpty(allErrors)) {
                errorMessage = allErrors.getFirst().getDefaultMessage();
            }
        } else {
            errorMessage = fieldError.getDefaultMessage();
        }
        return ApiResponse.error(CommonErrorCode.VALIDATE_FAILED, errorMessage);
    }
}
