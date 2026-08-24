package com.travis.infrastructure.framework.monitor.core;

import com.travis.infrastructure.common.monitor.error.ErrorReporter;
import com.travis.infrastructure.common.monitor.error.ErrorSource;
import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.scheduling.annotation.AsyncConfigurer;

/** 上报无返回值异步方法中未被调用方观察的异常。 */
@RequiredArgsConstructor
public class ErrorAsyncConfigurer implements AsyncConfigurer {

    private final ErrorReporter errorReporter;

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return this::handle;
    }

    private void handle(Throwable throwable, Method method, Object... params) {
        if (AnnotatedElementUtils.hasAnnotation(method, ApplicationModuleListener.class)) {
            return;
        }
        errorReporter.report(
                ErrorSource.ASYNC,
                method.getDeclaringClass().getName() + "#" + method.getName(),
                null,
                throwable);
    }
}
