package com.travis.infrastructure.framework.desensitize.core.annotation.slider;

import com.travis.infrastructure.framework.desensitize.core.annotation.SliderDesensitize;

import java.lang.annotation.*;

/**
 * 身份证
 *
 * @author travis
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@SliderDesensitize
public @interface IdCardDesensitize {

    /**
     * 前缀保留长度
     */
    int prefix() default 6;

    /**
     * 后缀保留长度
     */
    int suffix() default 4;

    /**
     * 替换规则
     */
    char mask() default '*';

    /**
     * 是否禁用脱敏
     * <p>
     * 支持 Spring EL 表达式，如果返回 true 则跳过脱敏
     */
    String disable() default "";

}
