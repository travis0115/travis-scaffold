package com.travis.infrastructure.framework.web.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/** 防止同一用户在短时间内重复提交相同请求。 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NoRepeatSubmit {

    /** 可选的 SpEL 业务 key；为空时使用方法参数摘要。 */
    String key() default "";

    /** 业务 key 的有效期。 */
    long interval() default 3;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /** 业务执行失败时是否删除防重 key，允许用户立即重试。 */
    boolean deleteOnFailure() default true;
}
