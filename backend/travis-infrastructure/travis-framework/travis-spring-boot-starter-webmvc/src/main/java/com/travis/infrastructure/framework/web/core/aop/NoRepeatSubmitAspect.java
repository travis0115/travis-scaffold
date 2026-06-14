package com.travis.infrastructure.framework.web.core.aop;

import com.travis.infrastructure.common.web.constant.MdcKey;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.infrastructure.framework.web.core.annotation.NoRepeatSubmit;
import com.travis.infrastructure.framework.web.core.util.IpUtil;
import com.travis.infrastructure.framework.web.core.util.ServletUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
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
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HexFormat;
import java.util.UUID;

/**
 * 防重复提交切面。
 *
 * <p>拦截标注 {@link NoRepeatSubmit} 的方法，根据目标类、目标方法、请求地址、用户标识和业务参数生成 Redis Key，并在注解指定的有效期内阻止重复执行。
 * 未登录请求使用客户端 IP 作为用户标识；未配置业务 Key 时，默认使用可序列化的方法参数摘要。
 *
 * <p>Redis Key 前缀通过 {@code travis.web.no-repeat-submit.key-prefix} 配置。
 *
 * @author travis
 */
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
@RequiredArgsConstructor
public class NoRepeatSubmitAspect {

    /** 方法参数名称解析器，用于构建 SpEL 上下文。 */
    private static final DefaultParameterNameDiscoverer PARAMETER_NAME_DISCOVERER =
            new DefaultParameterNameDiscoverer();

    /** SpEL 表达式解析器。 */
    private static final SpelExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    /** 仅当 Redis Key 仍属于当前请求时删除该 Key 的 Lua 脚本。 */
    private static final DefaultRedisScript<Long> DELETE_IF_OWNER_SCRIPT =
            new DefaultRedisScript<>(
                    "if redis.call('get', KEYS[1]) == ARGV[1] then "
                            + "return redis.call('del', KEYS[1]) else return 0 end",
                    Long.class);

    /** Redis 操作模板。 */
    private final StringRedisTemplate redisTemplate;

    /** 防重复提交 Redis Key 前缀。 */
    private final String keyPrefix;

    /**
     * 拦截防重复提交方法，在有效期内使用 Redis Key 保证同一请求只执行一次。
     *
     * @param joinPoint 方法连接点
     * @param noRepeatSubmit 防重复提交注解
     * @return 目标方法执行结果
     * @throws Throwable 目标方法执行异常
     */
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

    /**
     * 根据请求上下文、目标方法、用户标识和业务参数构建 Redis Key。
     *
     * @param joinPoint 方法连接点
     * @param annotation 防重复提交注解
     * @return Redis Key
     */
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
        return keyPrefix
                + method.getDeclaringClass().getName()
                + ":"
                + method.getName()
                + ":"
                + principal
                + ":"
                + sha256(requestKey);
    }

    /**
     * 解析防重复提交业务 Key，优先使用注解中的 SpEL，未配置时使用方法参数摘要。
     *
     * @param joinPoint 方法连接点
     * @param method 目标方法
     * @param annotation 防重复提交注解
     * @return 业务 Key
     */
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

    /**
     * 判断参数是否可以参与业务 Key 序列化。
     *
     * @param arg 方法参数
     * @return 可以序列化时返回 {@code true}
     */
    private boolean isSerializableArgument(Object arg) {
        return !(arg instanceof ServletRequest
                || arg instanceof ServletResponse
                || arg instanceof MultipartFile
                || arg instanceof BindingResult);
    }

    /**
     * 计算字符串的 SHA-256 摘要。
     *
     * @param value 原始字符串
     * @return 十六进制摘要
     */
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
