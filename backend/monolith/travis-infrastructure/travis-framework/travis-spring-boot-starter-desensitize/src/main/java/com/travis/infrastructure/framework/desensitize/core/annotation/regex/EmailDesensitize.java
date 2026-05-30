package com.travis.infrastructure.framework.desensitize.core.annotation.regex;

import com.travis.infrastructure.framework.desensitize.core.annotation.RegexDesensitize;
import java.lang.annotation.*;

/**
 * 邮箱脱敏注解
 *
 * @author travis
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@RegexDesensitize
public @interface EmailDesensitize {

    /**
     * 正则表达式
     */
    String regex() default "(^.)[^@]*(@.*$)";

    /**
     * 替换规则
     */
    String replacer() default "$1*****$2";

    /**
     * 是否禁用脱敏
     * <p>
     * 支持 Spring EL 表达式，如果返回 true 则跳过脱敏
     */
    String disable() default "";

}
