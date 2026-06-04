package com.travis.infrastructure.framework.satoken.core.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import com.travis.infrastructure.common.web.constant.MdcKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 *
 * @author Travis
 */
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        try {
            if (StpUtil.isLogin()) {
                MDC.put(MdcKeys.USER_ID, StpUtil.getLoginIdAsString());
            }
        } catch (Exception e) {
            // 未登录或不需要登录的接口，忽略
        }
        return true;
    }
}