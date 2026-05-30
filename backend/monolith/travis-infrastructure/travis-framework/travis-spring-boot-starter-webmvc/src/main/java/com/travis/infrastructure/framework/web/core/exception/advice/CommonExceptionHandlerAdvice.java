package com.travis.infrastructure.framework.web.core.exception.advice;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.travis.infrastructure.framework.web.core.exception.BizException;
import com.travis.infrastructure.framework.web.core.exception.CommonErrorCode;
import com.travis.infrastructure.framework.web.core.model.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * 全局异常处理器
 *
 * @author travis
 */
@RestControllerAdvice
@Slf4j
@NoArgsConstructor
public class CommonExceptionHandlerAdvice {

    /**
     * 处理 Sa-Token 未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public ApiResponse<?> handleNotLoginException(NotLoginException ex) {
        log.warn("未登录: {}", ex.getMessage());
        return ApiResponse.error(CommonErrorCode.UNAUTHORIZED);
    }

    /**
     * 处理 Sa-Token 无权限异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public ApiResponse<?> handleNotPermissionException(
            HttpServletRequest request, NotPermissionException ex) {
        log.warn("权限不足: {}", ex.getMessage());
        return ApiResponse.error(CommonErrorCode.FORBIDDEN);
    }

    /**
     * 处理 Sa-Token 无角色异常
     */
    @ExceptionHandler(NotRoleException.class)
    public ApiResponse<?> handleNotRoleException(
            HttpServletRequest request, NotRoleException ex) {
        log.warn("角色不匹配: {}", ex.getMessage());
        return ApiResponse.error(CommonErrorCode.FORBIDDEN);
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BizException.class)
    public ApiResponse<?> handleBizException(BizException ex) {
        log.error("业务异常: ", ex);
        return ApiResponse.error(ex);
    }

    /**
     * 兜底
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(Exception ex) {
        log.error("系统异常: ", ex);
        return ApiResponse.error(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

}
