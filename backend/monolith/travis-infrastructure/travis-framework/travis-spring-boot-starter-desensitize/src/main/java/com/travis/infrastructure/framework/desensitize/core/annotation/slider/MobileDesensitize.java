package com.travis.infrastructure.framework.desensitize.core.annotation.slider;

import com.travis.infrastructure.framework.desensitize.core.annotation.SliderDesensitize;

import java.lang.annotation.*;

/**
 * 手机号脱敏
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@SliderDesensitize
public @interface MobileDesensitize {

    int prefix() default 3;

    int suffix() default 4;

    char mask() default '*';

    String disable() default "";
}