package com.travis.infrastructure.common.web.constant;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

import org.springframework.core.Ordered;

/**
 * 异常处理器顺序的常量，保证异常处理器按照符合预期 考虑到多个 starter 都需要用到该工具类，所以放到 common 模块
 *
 * @author travis
 */
public final class ExceptionHandlerOrder {

    /** sa-token 异常处理器顺序，优先级：1 */
    public static final int SATOKEN_EXCEPTION = HIGHEST_PRECEDENCE;

    /** validation 异常处理器顺序，优先级：2 */
    public static final int VALIDATION_EXCEPTION = SATOKEN_EXCEPTION + CommonConstant.STEP;

    /** 业务异常处理器顺序，优先级：3 */
    public static final int BIZ_EXCEPTION = VALIDATION_EXCEPTION + CommonConstant.STEP;

    /** 服务端异常处理器顺序，包含最终兜底，必须放在最后，优先级：4 */
    public static final int SERVER_EXCEPTION = Ordered.LOWEST_PRECEDENCE;

    private ExceptionHandlerOrder() {}
}
