package com.travis.infrastructure.framework.satoken.core;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.travis.infrastructure.common.web.constant.ExceptionHandlerOrder;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.ApiResponse;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Sa-Token 异常处理器
 *
 * @author travis
 */
@RestControllerAdvice
@Order(ExceptionHandlerOrder.SATOKEN_EXCEPTION_HANDLER)
@Slf4j
@NoArgsConstructor
public class SaTokenExceptionHandler {

    /** 未登录异常 */
    @ExceptionHandler(NotLoginException.class)
    public ApiResponse<?> handleNotLoginException(NotLoginException ex) {
        log.warn("未登录: {}", ex.getMessage());
        return ApiResponse.error(CommonErrorCode.UNAUTHORIZED);
    }

    /** 无权限异常 */
    @ExceptionHandler(NotPermissionException.class)
    public ApiResponse<?> handleNotPermissionException(NotPermissionException ex) {
        log.warn("权限不足: {}", ex.getMessage());
        return ApiResponse.error(CommonErrorCode.FORBIDDEN);
    }

    /** 无角色异常 */
    @ExceptionHandler(NotRoleException.class)
    public ApiResponse<?> handleNotRoleException(NotRoleException ex) {
        log.warn("角色不匹配: {}", ex.getMessage());
        return ApiResponse.error(CommonErrorCode.FORBIDDEN);
    }
}
