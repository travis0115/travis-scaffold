package com.travis.infrastructure.framework.desensitize.core.annotation;

import java.lang.annotation.*;

/**
 * 滑动脱敏策略注解
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.RECORD_COMPONENT, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@DesensitizeBy
public @interface SliderDesensitize {

    int prefix() default 0;

    int suffix() default 0;

    char mask() default '*';

    String disable() default "";
}