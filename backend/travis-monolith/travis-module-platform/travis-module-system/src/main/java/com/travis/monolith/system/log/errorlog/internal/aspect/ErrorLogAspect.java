package com.travis.monolith.system.log.errorlog.internal.aspect;

import com.travis.infrastructure.common.web.enums.LoginType;
import com.travis.infrastructure.framework.satoken.core.StpKit;
import com.travis.infrastructure.framework.web.core.util.IpUtil;
import com.travis.monolith.system.log.errorlog.internal.entity.SysErrorLog;
import com.travis.monolith.system.log.errorlog.internal.service.SysErrorLogService;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ErrorLogAspect {
    private static final int MAX_STACK_TRACE_LENGTH = 16000;
    private static final Set<Throwable> RECORDED_EXCEPTIONS =
            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    private final SysErrorLogService errorLogService;

    @AfterThrowing(
            pointcut =
                    "execution(* com.travis.monolith..*(..))"
                            + " && (@within(org.springframework.stereotype.Service)"
                            + " || @within(org.springframework.stereotype.Component)"
                            + " || @within(org.springframework.web.bind.annotation.RestController))"
                            + " && !within(com.travis.monolith.system.log.errorlog..*)",
            throwing = "exception")
    public void record(JoinPoint joinPoint, Throwable exception) {
        if (!RECORDED_EXCEPTIONS.add(exception)) {
            return;
        }
        try {
            var errorLog = new SysErrorLog();
            var requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes servletAttributes) {
                var request = servletAttributes.getRequest();
                errorLog.setRequestUrl(request.getRequestURI());
                errorLog.setRequestMethod(request.getMethod());
                errorLog.setIp(IpUtil.getClientIp(request));
                var logic = StpKit.of(LoginType.ADMIN);
                if (logic.isLogin()) {
                    errorLog.setUserId(logic.getLoginIdAsLong());
                }
            }
            errorLog.setControllerMethod(joinPoint.getSignature().toLongString());
            errorLog.setExceptionClass(exception.getClass().getName());
            errorLog.setMessage(exception.getMessage());
            errorLog.setStackTrace(stackTrace(exception));
            errorLogService.saveError(errorLog);
        } catch (Exception saveException) {
            log.error("错误日志写入失败", saveException);
        }
    }

    private String stackTrace(Throwable exception) {
        var writer = new StringWriter();
        exception.printStackTrace(new PrintWriter(writer));
        String stackTrace = writer.toString();
        return stackTrace.length() <= MAX_STACK_TRACE_LENGTH
                ? stackTrace
                : stackTrace.substring(0, MAX_STACK_TRACE_LENGTH);
    }
}
