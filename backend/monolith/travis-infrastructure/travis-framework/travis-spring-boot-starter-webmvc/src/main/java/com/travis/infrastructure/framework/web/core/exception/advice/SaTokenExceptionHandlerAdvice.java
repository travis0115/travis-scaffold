package com.travis.infrastructure.framework.web.core.exception.advice;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.travis.infrastructure.framework.web.core.exception.CommonErrorCode;
import com.travis.infrastructure.framework.web.core.model.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * Sa-Token 异常处理器
 *
 * @author travis
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@Slf4j
@NoArgsConstructor
public class SaTokenExceptionHandlerAdvice {

    /**
     * 未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public ApiResponse<?> handleNotLoginException(NotLoginException ex) {
        log.warn("未登录: {}", ex.getMessage());
        return ApiResponse.error(CommonErrorCode.UNAUTHORIZED);
    }

    /**
     * 无权限异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public ApiResponse<?> handleNotPermissionException(
            HttpServletRequest request, NotPermissionException ex) {
        log.warn("权限不足: {}", ex.getMessage());
        return ApiResponse.error(CommonErrorCode.FORBIDDEN);
    }

    /**
     * 无角色异常
     */
    @ExceptionHandler(NotRoleException.class)
    public ApiResponse<?> handleNotRoleException(
            HttpServletRequest request, NotRoleException ex) {
        log.warn("角色不匹配: {}", ex.getMessage());
        return ApiResponse.error(CommonErrorCode.FORBIDDEN);
    }
}
