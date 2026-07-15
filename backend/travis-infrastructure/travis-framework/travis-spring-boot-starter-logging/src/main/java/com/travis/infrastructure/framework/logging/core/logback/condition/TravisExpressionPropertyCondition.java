package com.travis.infrastructure.framework.logging.core.logback.condition;

import ch.qos.logback.core.boolex.ExpressionPropertyCondition;
import cn.hutool.core.util.StrUtil;

/** 自定义logback表达式条件 用于xml配置文件 */
public class TravisExpressionPropertyCondition extends ExpressionPropertyCondition {

    private static final String PROPERTY_EQUALS_IGNORE_CASE_FUNCTION_KEY =
            "propertyEqualsIgnoreCase";

    public TravisExpressionPropertyCondition() {
        biFunctionMap.put(PROPERTY_EQUALS_IGNORE_CASE_FUNCTION_KEY, this::propertyEqualsIgnoreCase);
    }

    /** 忽略大小写比较 Logback 上下文属性与期望值。 */
    public boolean propertyEqualsIgnoreCase(String propertyKey, String value) {
        return StrUtil.equalsIgnoreCase(property(propertyKey), value);
    }
}
