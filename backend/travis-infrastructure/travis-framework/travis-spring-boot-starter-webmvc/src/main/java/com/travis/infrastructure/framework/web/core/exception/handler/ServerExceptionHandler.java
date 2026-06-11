package com.travis.infrastructure.framework.web.core.exception.handler;

import com.travis.infrastructure.common.web.constant.ExceptionHandlerOrder;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.ApiResponse;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.FileNotFoundException;

/**
 * Server 异常处理器
 *
 * @author Travis
 */
@RestControllerAdvice
@Slf4j
@Order(ExceptionHandlerOrder.SERVER_EXCEPTION_HANDLER)
@NoArgsConstructor
public class ServerExceptionHandler {
    /** 算数异常 */
    @ExceptionHandler(IllegalStateException.class)
    public ApiResponse<?> handleIllegalStateException(IllegalStateException ex) {
        log.error("状态异常：", ex);
        return ApiResponse.error(CommonErrorCode.ILLEGALSTATE_EXCEPTION);
    }

    /** 算数异常 */
    @ExceptionHandler(ArithmeticException.class)
    public ApiResponse<?> handleArithmeticException(ArithmeticException ex) {
        log.error("算数异常：", ex);
        return ApiResponse.error(CommonErrorCode.ARITHMETIC_EXCEPTION);
    }

    /** 中断异常 */
    @ExceptionHandler(InterruptedException.class)
    public ApiResponse<?> handleInterruptedException(InterruptedException ex) {
        log.warn("中断异常：", ex);
        return ApiResponse.error(CommonErrorCode.INTERRUPTED);
    }

    /** 上传文件大小超出限制 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ApiResponse<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.warn("上传文件大小超过限制：", ex);
        return ApiResponse.error(CommonErrorCode.FILE_TOO_LARGE);
    }

    /** 文件或目录未找到 */
    @ExceptionHandler(FileNotFoundException.class)
    public ApiResponse<?> handleFileNotFoundException(MaxUploadSizeExceededException ex) {
        log.warn("文件或目录未找到：", ex);
        return ApiResponse.error(CommonErrorCode.FILE_NOT_FOUND);
    }

    /**
     * SpringMVC 请求地址不存在
     *
     * <p>注意，它需要设置如下两个配置项： 1. spring.mvc.throw-exception-if-no-handler-found 为 true 2.
     * spring.mvc.static-path-pattern 为 /statics/**
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ApiResponse<?> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        log.warn("请求地址不存在：", ex);
        return ApiResponse.error(CommonErrorCode.NOT_FOUND);
    }

    /**
     * SpringMVC 请求方法不正确
     *
     * <p>例如说，A 接口的方法为 GET 方式，结果请求方法为 POST 方式，导致不匹配
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResponse<?> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex) {
        log.warn("请求方法不正确：", ex);
        return ApiResponse.error(CommonErrorCode.METHOD_NOT_ALLOWED);
    }

    /**
     * SpringMVC 请求的 Content-Type 不正确
     *
     * <p>例如说，A 接口的 Content-Type 为 application/json，结果请求的 Content-Type 为
     * application/octet-stream，导致不匹配
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ApiResponse<?> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException ex) {
        log.warn("请求的 Content-Type 不正确：", ex);
        return ApiResponse.error(CommonErrorCode.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * SpringMVC 响应序列化失败
     *
     * <p>例如说，返回的对象无法被 Jackson 序列化为 JSON
     */
    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ApiResponse<?> handleHttpMessageNotWritableException(
            HttpMessageNotWritableException ex) {
        log.error("响应序列化失败：", ex);
        return ApiResponse.error(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    /**
     * SpringMVC 静态资源未找到（Spring 6.2+）
     *
     * <p>当请求路径对应的静态资源不存在时抛出，区别于 NoHandlerFoundException
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ApiResponse<?> handleNoResourceFoundException(NoResourceFoundException ex) {
        if (!"favicon.ico".equals(ex.getResourcePath())) {
            log.warn("资源未找到：", ex);
        }
        return ApiResponse.error(CommonErrorCode.NOT_FOUND);
    }

    /** 兜底 */
    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(Exception ex) {
        log.error("系统异常: ", ex);
        return ApiResponse.error(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }
}
