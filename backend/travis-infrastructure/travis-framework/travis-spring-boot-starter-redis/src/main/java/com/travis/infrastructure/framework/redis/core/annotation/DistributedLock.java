package com.travis.infrastructure.framework.redis.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/** 基于 Redisson 的分布式锁。 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    /** 锁的业务 key，支持 SpEL，例如 {@code #orderId}、{@code #req.userId}。 */
    String key();

    /** 方法级命名空间；为空时使用类上的 {@link DistributedLockNamespace}。 */
    String namespace() default "";

    /** 等待获取锁的时间，默认立即返回。 */
    long waitTime() default 0;

    /** 锁持有时间，负数表示使用 Redisson watchdog 自动续期。 */
    long leaseTime() default -1;

    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
