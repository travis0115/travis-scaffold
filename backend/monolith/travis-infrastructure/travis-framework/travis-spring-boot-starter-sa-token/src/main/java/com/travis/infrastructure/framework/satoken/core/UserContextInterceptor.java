package com.travis.infrastructure.framework.satoken.core;

import com.travis.infrastructure.common.web.constant.MdcKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户上下文拦截器，遍历所有已注册的 StpLogic 实例，
 * 找到当前已登录的用户并将其 ID 写入 MDC。
 *
 * @author Travis
 */
public class UserContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        try {
            for (var logic : StpKit.all()) {
                if (logic.isLogin()) {
                    MDC.put(MdcKeys.USER_ID, logic.getLoginIdAsString());
                    break;
                }
            }
        } catch (Exception e) {
            // 未登录或不需要登录的接口，忽略
        }
        return true;
    }
}