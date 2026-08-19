package com.travis.infrastructure.framework.monitor.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.travis.infrastructure.common.monitor.error.ErrorEvent;
import com.travis.infrastructure.common.monitor.error.ErrorReporter;
import com.travis.infrastructure.common.monitor.error.ErrorSource;
import com.travis.infrastructure.framework.event.core.TransactionalApplicationEventPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ModulithErrorReporterTest {

    @Test
    void shouldPublishInRequiresNewTransaction() {
        var publisher = mock(TransactionalApplicationEventPublisher.class);
        var reporter = new ModulithErrorReporter(publisher);
        var event = ErrorEvent.builder().sourceType(ErrorSource.WEB).build();

        reporter.report(event);

        verify(publisher).publishEventRequiresNew(event);
    }

    @Test
    void shouldNotReplaceOriginalFailureWhenPublicationFails() {
        var publisher = mock(TransactionalApplicationEventPublisher.class);
        org.mockito.Mockito.doThrow(new IllegalStateException("database unavailable"))
                .when(publisher)
                .publishEventRequiresNew(any());
        var reporter = new ModulithErrorReporter(publisher);

        assertThatCode(
                        () ->
                                reporter.report(
                                        ErrorEvent.builder().sourceType(ErrorSource.WEB).build()))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldKeepStackTraceUpToConfiguredLimit() {
        var publisher = mock(TransactionalApplicationEventPublisher.class);
        var reporter = new ModulithErrorReporter(publisher);
        var oversized = "x".repeat(ErrorReporter.MAX_STACK_TRACE_LENGTH + 100);

        reporter.report(
                ErrorEvent.builder().sourceType(ErrorSource.WEB).stackTrace(oversized).build());

        var event = ArgumentCaptor.forClass(ErrorEvent.class);
        verify(publisher).publishEventRequiresNew(event.capture());
        assertThat(event.getValue().stackTrace())
                .hasSize(ErrorReporter.MAX_STACK_TRACE_LENGTH)
                .isEqualTo(oversized.substring(0, ErrorReporter.MAX_STACK_TRACE_LENGTH));
    }
}
