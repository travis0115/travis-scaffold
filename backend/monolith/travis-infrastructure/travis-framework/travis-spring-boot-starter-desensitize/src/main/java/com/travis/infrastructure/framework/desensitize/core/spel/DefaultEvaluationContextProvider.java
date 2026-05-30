package com.travis.infrastructure.framework.desensitize.core.spel;

import cn.hutool.extra.spring.SpringUtil;
import org.slf4j.MDC;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;


/**
 * 默认的 SpEL 上下文提供者
 */
public class DefaultEvaluationContextProvider implements EvaluationContextProvider {

    @Override
    public EvaluationContext getContext() {

        var context = new StandardEvaluationContext();

        // MDC 上下文变量
        context.setVariable("mdc", MDC.getCopyOfContextMap());

        // Spring Bean 访问能力
        context.setBeanResolver(new BeanFactoryResolver(SpringUtil.getApplicationContext()));

        return context;
    }
}
