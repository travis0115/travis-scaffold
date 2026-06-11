package com.travis.infrastructure.framework.redis.core.aop;

import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.framework.redis.core.annotation.DistributedLock;
import com.travis.infrastructure.framework.redis.core.annotation.DistributedLockNamespace;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RedissonClient;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;

/** 分布式锁切面。顺序高于事务切面，确保事务完成后再释放锁。 */
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class DistributedLockAspect {

    private static final String KEY_PREFIX = "travis:lock:";
    private static final DefaultParameterNameDiscoverer PARAMETER_NAME_DISCOVERER =
            new DefaultParameterNameDiscoverer();
    private static final SpelExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    private final RedissonClient redissonClient;

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock)
            throws Throwable {
        String lockKey = resolveLockKey(joinPoint, distributedLock);
        var lock = redissonClient.getLock(lockKey);
        boolean acquired = false;
        try {
            acquired = tryLock(lock, distributedLock);
            if (!acquired) {
                throw new BizException(CommonErrorCode.DISTRIBUTED_LOCK_FAILED);
            }
            return joinPoint.proceed();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException(CommonErrorCode.INTERRUPTED, e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private boolean tryLock(org.redisson.api.RLock lock, DistributedLock annotation)
            throws InterruptedException {
        if (annotation.leaseTime() < 0) {
            return lock.tryLock(annotation.waitTime(), annotation.timeUnit());
        }
        return lock.tryLock(annotation.waitTime(), annotation.leaseTime(), annotation.timeUnit());
    }

    private String resolveLockKey(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method =
                AopUtils.getMostSpecificMethod(
                        signature.getMethod(), joinPoint.getTarget().getClass());
        var context =
                new MethodBasedEvaluationContext(
                        joinPoint.getTarget(),
                        method,
                        joinPoint.getArgs(),
                        PARAMETER_NAME_DISCOVERER);
        Object keyValue;
        try {
            keyValue = EXPRESSION_PARSER.parseExpression(distributedLock.key()).getValue(context);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("分布式锁 key 表达式解析失败: " + distributedLock.key(), e);
        }
        if (keyValue == null || !StringUtils.hasText(keyValue.toString())) {
            throw new IllegalArgumentException("分布式锁 key 不能为空");
        }

        String namespace = distributedLock.namespace();
        if (!StringUtils.hasText(namespace)) {
            var namespaceAnnotation =
                    AnnotatedElementUtils.findMergedAnnotation(
                            joinPoint.getTarget().getClass(), DistributedLockNamespace.class);
            if (namespaceAnnotation != null) {
                namespace = namespaceAnnotation.value();
            }
        }
        if (!StringUtils.hasText(namespace)) {
            namespace = method.getDeclaringClass().getName() + ":" + method.getName();
        }
        return KEY_PREFIX + namespace + ":" + keyValue;
    }
}
