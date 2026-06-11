package com.travis.infrastructure.framework.web.core.aop;

import com.travis.infrastructure.common.web.constant.MdcKey;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.infrastructure.framework.web.core.annotation.NoRepeatSubmit;
import com.travis.infrastructure.framework.web.core.annotation.NoRepeatSubmitNamespace;
import com.travis.infrastructure.framework.web.core.exception.BizException;
import com.travis.infrastructure.framework.web.core.util.IpUtil;
import com.travis.infrastructure.framework.web.core.util.ServletUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HexFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

/** 防重复提交切面。 */
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
@RequiredArgsConstructor
public class NoRepeatSubmitAspect {

    private static final String KEY_PREFIX = "travis:repeat-submit:";
    private static final DefaultParameterNameDiscoverer PARAMETER_NAME_DISCOVERER =
            new DefaultParameterNameDiscoverer();
    private static final SpelExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();
    private static final DefaultRedisScript<Long> DELETE_IF_OWNER_SCRIPT =
            new DefaultRedisScript<>(
                    "if redis.call('get', KEYS[1]) == ARGV[1] then "
                            + "return redis.call('del', KEYS[1]) else return 0 end",
                    Long.class);

    private final StringRedisTemplate redisTemplate;

    @Around("@annotation(noRepeatSubmit)")
    public Object around(ProceedingJoinPoint joinPoint, NoRepeatSubmit noRepeatSubmit)
            throws Throwable {
        String redisKey = buildRedisKey(joinPoint, noRepeatSubmit);
        String requestId = UUID.randomUUID().toString();
        Boolean acquired =
                redisTemplate
                        .opsForValue()
                        .setIfAbsent(
                                redisKey,
                                requestId,
                                noRepeatSubmit.interval(),
                                noRepeatSubmit.timeUnit());
        if (!Boolean.TRUE.equals(acquired)) {
            throw new BizException(CommonErrorCode.REPEATED_REQUEST);
        }
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            if (noRepeatSubmit.deleteOnFailure()) {
                redisTemplate.execute(
                        DELETE_IF_OWNER_SCRIPT, Collections.singletonList(redisKey), requestId);
            }
            throw e;
        }
    }

    private String buildRedisKey(ProceedingJoinPoint joinPoint, NoRepeatSubmit annotation) {
        var request = ServletUtil.getRequest();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method =
                AopUtils.getMostSpecificMethod(
                        signature.getMethod(), joinPoint.getTarget().getClass());
        String principal = MDC.get(MdcKey.USER_ID);
        if (!StringUtils.hasText(principal)) {
            principal = IpUtil.getClientIp(request);
        }
        String requestKey =
                request.getMethod()
                        + ":"
                        + request.getRequestURI()
                        + ":"
                        + resolveBusinessKey(joinPoint, method, annotation);
        return KEY_PREFIX
                + resolveNamespace(joinPoint, method)
                + ":"
                + principal
                + ":"
                + sha256(requestKey);
    }

    private String resolveNamespace(ProceedingJoinPoint joinPoint, Method method) {
        var annotation =
                AnnotatedElementUtils.findMergedAnnotation(
                        joinPoint.getTarget().getClass(), NoRepeatSubmitNamespace.class);
        return annotation != null
                ? annotation.value()
                : method.getDeclaringClass().getName() + ":" + method.getName();
    }

    private String resolveBusinessKey(
            ProceedingJoinPoint joinPoint, Method method, NoRepeatSubmit annotation) {
        if (StringUtils.hasText(annotation.key())) {
            var context =
                    new MethodBasedEvaluationContext(
                            joinPoint.getTarget(),
                            method,
                            joinPoint.getArgs(),
                            PARAMETER_NAME_DISCOVERER);
            Object value = EXPRESSION_PARSER.parseExpression(annotation.key()).getValue(context);
            if (value == null || !StringUtils.hasText(value.toString())) {
                throw new IllegalArgumentException("防重复提交 key 不能为空");
            }
            return value.toString();
        }
        Object[] args =
                Arrays.stream(joinPoint.getArgs()).filter(this::isSerializableArgument).toArray();
        return JsonUtil.toJsonString(args);
    }

    private boolean isSerializableArgument(Object arg) {
        return !(arg instanceof ServletRequest
                || arg instanceof ServletResponse
                || arg instanceof MultipartFile
                || arg instanceof BindingResult);
    }

    private String sha256(String value) {
        try {
            byte[] digest =
                    MessageDigest.getInstance("SHA-256")
                            .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前运行环境不支持 SHA-256", e);
        }
    }
}
