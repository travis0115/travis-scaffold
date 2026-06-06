package com.travis.infrastructure.common.web.constant;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

/**
 * 过滤器顺序的常量，保证过滤器按照符合预期 考虑到多个 starter 都需要用到该工具类，所以放到 common 模块
 *
 * @author travis
 */
public final class WebFilterOrder {

    /**
     * CORS 优先级必须在Security之前 Spring Security Filter 默认为 -100，可见
     * org.springframework.boot.autoconfigure.security.SecurityProperties 配置属性类
     */
    public static final int CORS_FILTER = HIGHEST_PRECEDENCE;

    /** 请求上下文过滤器 优先级必须在其他过滤器之前 */
    public static final int REQUEST_CONTEXT_FILTER = CORS_FILTER + CommonConstant.STEP;

    /** MDC */
    public static final int MDC_FILTER = REQUEST_CONTEXT_FILTER + CommonConstant.STEP;

    /** AccessLog 在REQUEST_ID_FILTER之后 */
    public static final int ACCESS_LOG_FILTER = MDC_FILTER + CommonConstant.STEP;

    private WebFilterOrder() {}
}
