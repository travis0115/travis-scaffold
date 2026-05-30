package com.travis.infrastructure.framework.desensitize.core.spel;

import org.springframework.expression.EvaluationContext;

/**
 * SpEL 上下文提供者 接口
 */
public interface EvaluationContextProvider {
    /**
     * 构建 SpEL EvaluationContext
     */
    EvaluationContext getContext();
}
