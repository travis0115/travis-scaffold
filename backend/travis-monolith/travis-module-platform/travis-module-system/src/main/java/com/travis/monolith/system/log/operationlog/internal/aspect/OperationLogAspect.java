package com.travis.monolith.system.log.operationlog.internal.aspect;

import com.travis.infrastructure.common.logging.annotation.OperationLog;
import com.travis.infrastructure.common.logging.annotation.OperationLogModule;
import com.travis.infrastructure.common.web.constant.MdcKey;
import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.infrastructure.framework.web.core.util.IpUtil;
import com.travis.infrastructure.framework.web.core.util.ServletUtil;
import com.travis.monolith.system.log.operationlog.api.event.OperationLogEvent;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.util.Arrays;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/** 采集带有 {@link OperationLog} 注解的后台操作并发布异步落库事件。 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class OperationLogAspect {

    private static final int MAX_CONTENT_LENGTH = 16000;
    private static final Set<String> SENSITIVE_FIELDS =
            Set.of(
                    "password",
                    "oldpassword",
                    "newpassword",
                    "token",
                    "authorization",
                    "secret",
                    "secretkey");

    private final ApplicationEventPublisher eventPublisher;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog)
            throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Throwable failure = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            failure = e;
            throw e;
        } finally {
            try {
                publishEvent(joinPoint, operationLog, result, failure, startTime);
            } catch (Exception e) {
                log.error("操作日志事件发布失败", e);
            }
        }
    }

    private void publishEvent(
            ProceedingJoinPoint joinPoint,
            OperationLog operationLog,
            Object result,
            Throwable failure,
            long startTime) {
        var request = ServletUtil.getRequest();
        var signature = (MethodSignature) joinPoint.getSignature();
        var moduleAnnotation =
                AnnotatedElementUtils.findMergedAnnotation(
                        joinPoint.getTarget().getClass(), OperationLogModule.class);
        eventPublisher.publishEvent(
                new OperationLogEvent(
                        currentUserId(),
                        operationLog.action(),
                        moduleAnnotation != null
                                ? moduleAnnotation.value()
                                : joinPoint.getTarget().getClass().getSimpleName(),
                        signature.getDeclaringTypeName() + "#" + signature.getName(),
                        request.getRequestURI(),
                        request.getMethod(),
                        operationLog.recordRequest()
                                ? serialize(
                                        Arrays.stream(joinPoint.getArgs())
                                                .filter(this::isSerializableArgument)
                                                .toArray())
                                : null,
                        operationLog.recordResponse() ? serialize(result) : null,
                        IpUtil.getClientIp(request),
                        System.currentTimeMillis() - startTime,
                        failure == null ? 1 : 0,
                        failure == null ? null : truncate(failure.getMessage())));
    }

    private Long currentUserId() {
        String userId = MDC.get(MdcKey.USER_ID);
        if (userId == null) {
            return null;
        }
        try {
            return Long.valueOf(userId);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private boolean isSerializableArgument(Object arg) {
        return !(arg instanceof ServletRequest
                || arg instanceof ServletResponse
                || arg instanceof MultipartFile
                || arg instanceof BindingResult);
    }

    private String serialize(Object value) {
        if (value == null) {
            return null;
        }
        try {
            JsonNode tree = JsonUtil.getObjectMapper().valueToTree(value);
            maskSensitiveFields(tree);
            return truncate(JsonUtil.toJsonString(tree));
        } catch (RuntimeException ignored) {
            return "[无法序列化]";
        }
    }

    private void maskSensitiveFields(JsonNode node) {
        if (node instanceof ObjectNode objectNode) {
            for (var entry : objectNode.properties()) {
                if (SENSITIVE_FIELDS.contains(entry.getKey().toLowerCase())) {
                    objectNode.set(
                            entry.getKey(),
                            JsonUtil.getObjectMapper().getNodeFactory().textNode("******"));
                } else {
                    maskSensitiveFields(entry.getValue());
                }
            }
        } else if (node instanceof ArrayNode arrayNode) {
            for (JsonNode child : arrayNode.elements()) {
                maskSensitiveFields(child);
            }
        }
    }

    private String truncate(String value) {
        if (value == null || value.length() <= MAX_CONTENT_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_CONTENT_LENGTH);
    }
}
