package com.travis.monolith.system.log.operationlog.internal.aspect;

import com.travis.infrastructure.common.logging.annotation.OperationLog;
import com.travis.infrastructure.common.logging.annotation.OperationLogModule;
import com.travis.infrastructure.common.logging.enums.OperationBusinessType;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.constant.MdcKey;
import com.travis.infrastructure.framework.desensitize.core.Desensitizer;
import com.travis.infrastructure.framework.event.core.TransactionalApplicationEventPublisher;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.infrastructure.framework.web.core.util.IpUtil;
import com.travis.infrastructure.framework.web.core.util.ServletUtil;
import com.travis.infrastructure.framework.web.core.util.UserAgentUtil;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.log.operationlog.api.event.OperationLogEvent;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

/** 采集带有 {@link OperationLog} 注解的后台操作并发布异步落库事件。 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class OperationLogAspect {

    /** 单个请求或响应日志内容允许记录的最大字符数。 */
    private static final int MAX_CONTENT_LENGTH = 16000;

    private final TransactionalApplicationEventPublisher eventPublisher;
    private final Desensitizer desensitizer;

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
        if (request != null) {
            var uaInfo = UserAgentUtil.getCurrentUserAgentInfo(request);
            var event =
                    OperationLogEvent.builder()
                            .userId(StpKit.getLoginIdAsLong(LoginType.ADMIN))
                            .description(operationLog.action())
                            .module(
                                    moduleAnnotation != null
                                            ? moduleAnnotation.value()
                                            : joinPoint.getTarget().getClass().getSimpleName())
                            .businessType(
                                    resolveBusinessType(
                                            operationLog.businessType(),
                                            operationLog.action(),
                                            request.getMethod()))
                            .method(signature.getDeclaringTypeName() + "#" + signature.getName())
                            .requestUrl(request.getRequestURI())
                            .requestMethod(request.getMethod())
                            .requestParams(
                                    operationLog.recordRequest()
                                            ? serialize(
                                                    Arrays.stream(joinPoint.getArgs())
                                                            .filter(this::isSerializableArgument)
                                                            .toArray())
                                            : null)
                            .responseResult(
                                    operationLog.recordResponse() ? serialize(result) : null)
                            .requestId(MDC.get(MdcKey.REQUEST_ID))
                            .ip(IpUtil.getClientIp(request))
                            .userAgent(uaInfo.getUserAgent())
                            .browser(uaInfo.getBrowser())
                            .os(uaInfo.getOs())
                            .duration(System.currentTimeMillis() - startTime)
                            .status(
                                    failure == null
                                            ? Status.ENABLED.getValue()
                                            : Status.DISABLED.getValue())
                            .errorMsg(failure == null ? null : truncate(failure.getMessage()))
                            .build();
            eventPublisher.publishEvent(event);
        }
    }

    private String resolveBusinessType(
            OperationBusinessType businessType, String action, String requestMethod) {
        if (businessType != OperationBusinessType.AUTO) {
            return businessType.name();
        }
        if (action != null) {
            if (action.contains("删除") || action.contains("清理")) {
                return OperationBusinessType.DELETE.name();
            }
            if (action.contains("新增") || action.contains("复制")) {
                return OperationBusinessType.CREATE.name();
            }
            if (action.contains("上传")) {
                return OperationBusinessType.UPLOAD.name();
            }
            if (action.contains("导入")) {
                return OperationBusinessType.IMPORT.name();
            }
            if (action.contains("导出")) {
                return OperationBusinessType.EXPORT.name();
            }
            if (action.contains("分配") || action.contains("授权")) {
                return OperationBusinessType.GRANT.name();
            }
            if (action.contains("修改")
                    || action.contains("更新")
                    || action.contains("重置")
                    || action.contains("设置")
                    || action.contains("启停")
                    || action.contains("启动")
                    || action.contains("停止")
                    || action.contains("关闭")
                    || action.contains("撤回")
                    || action.contains("上移")
                    || action.contains("下移")) {
                return OperationBusinessType.UPDATE.name();
            }
        }
        return switch (requestMethod) {
            case Method.POST -> OperationBusinessType.CREATE.name();
            case Method.PUT, Method.PATCH -> OperationBusinessType.UPDATE.name();
            case Method.DELETE -> OperationBusinessType.DELETE.name();
            default -> OperationBusinessType.OTHER.name();
        };
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
        var serialized = desensitizer.desensitizeObject(value);
        return serialized == null ? "[无法序列化]" : truncate(serialized);
    }

    private String truncate(String value) {
        if (value == null || value.length() <= MAX_CONTENT_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_CONTENT_LENGTH);
    }
}
