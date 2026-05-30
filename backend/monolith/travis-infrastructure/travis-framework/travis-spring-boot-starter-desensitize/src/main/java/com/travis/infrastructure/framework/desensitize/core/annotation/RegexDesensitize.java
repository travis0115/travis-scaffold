package com.travis.infrastructure.framework.desensitize.core.annotation;

import java.lang.annotation.*;

/**
 * 正则脱敏策略注解
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.RECORD_COMPONENT, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@DesensitizeBy
public @interface RegexDesensitize {

    String regex() default "^[\\s\\S]*$";

    String replacer() default "*****";

    String disable() default "";
}