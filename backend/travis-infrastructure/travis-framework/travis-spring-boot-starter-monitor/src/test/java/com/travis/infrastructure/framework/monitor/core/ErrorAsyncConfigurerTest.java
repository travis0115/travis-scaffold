package com.travis.infrastructure.framework.monitor.core;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.travis.infrastructure.common.monitor.error.ErrorReporter;
import com.travis.infrastructure.common.monitor.error.ErrorSource;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.events.ApplicationModuleListener;

class ErrorAsyncConfigurerTest {

    @Test
    void shouldIgnoreApplicationModuleListenerFailure() throws NoSuchMethodException {
        var reporter = mock(ErrorReporter.class);
        var handler = new ErrorAsyncConfigurer(reporter).getAsyncUncaughtExceptionHandler();
        var method = TestMethods.class.getDeclaredMethod("moduleListener");

        handler.handleUncaughtException(new IllegalStateException("failed"), method);

        verifyNoInteractions(reporter);
    }

    @Test
    void shouldReportOtherAsyncFailure() throws NoSuchMethodException {
        var reporter = mock(ErrorReporter.class);
        var handler = new ErrorAsyncConfigurer(reporter).getAsyncUncaughtExceptionHandler();
        var method = TestMethods.class.getDeclaredMethod("asyncMethod");
        var throwable = new IllegalStateException("failed");

        handler.handleUncaughtException(throwable, method);

        verify(reporter)
                .report(
                        ErrorSource.ASYNC,
                        TestMethods.class.getName() + "#asyncMethod",
                        null,
                        throwable);
    }

    private static class TestMethods {

        @ModuleListener
        void moduleListener() {}

        void asyncMethod() {}
    }

    @ApplicationModuleListener
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface ModuleListener {}
}
