package com.travis.infrastructure.common.web.constant;

import org.springframework.core.Ordered;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

/**
 * 异常处理器顺序的常量，保证异常处理器按照符合预期 考虑到多个 starter 都需要用到该工具类，所以放到 common 模块
 *
 * @author travis
 */
public final class ExceptionHandlerOrder {

    /** sa-token 异常处理器顺序 */
    public static final int SATOKEN_EXCEPTION_HANDLER = HIGHEST_PRECEDENCE;

    /** server 异常处理器顺序 */
    public static final int SERVER_EXCEPTION_HANDLER =
            SATOKEN_EXCEPTION_HANDLER + CommonConstant.STEP;

    /** validation 异常处理器顺序 */
    public static final int VALIDATION_EXCEPTION_HANDLER =
            SERVER_EXCEPTION_HANDLER + CommonConstant.STEP;

    /** 兜底异常处理器，放在最后 */
    public static final int Biz_EXCEPTION_HANDLER = Ordered.LOWEST_PRECEDENCE;

    private ExceptionHandlerOrder() {}
}
