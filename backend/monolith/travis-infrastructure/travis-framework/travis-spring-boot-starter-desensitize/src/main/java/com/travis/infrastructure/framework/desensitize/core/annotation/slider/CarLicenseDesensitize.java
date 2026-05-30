package com.travis.infrastructure.framework.desensitize.core.annotation.slider;

import com.travis.infrastructure.framework.desensitize.core.annotation.SliderDesensitize;

import java.lang.annotation.*;

/**
 * 车牌号
 *
 * @author travis
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@SliderDesensitize
public @interface CarLicenseDesensitize {

    /**
     * 前缀保留长度
     */
    int prefix() default 3;

    /**
     * 后缀保留长度
     */
    int suffix() default 1;

    /**
     * 替换规则，车牌号;比如：粤A66666 脱敏之后为粤A6***6
     */
    char mask() default '*';

    /**
     * 是否禁用脱敏
     * <p>
     * 支持 Spring EL 表达式，如果返回 true 则跳过脱敏
     */
    String disable() default "";
}
