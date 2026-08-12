package com.travis.infrastructure.framework.monitor.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.travis.infrastructure.common.monitor.error.ErrorEvent;
import com.travis.infrastructure.common.monitor.error.ErrorSource;
import com.travis.infrastructure.framework.event.core.TransactionalApplicationEventPublisher;
import com.travis.infrastructure.framework.jackson.core.JsonUtil;
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
    void shouldKeepSerializedEventWithinPublicationColumnLimit() {
        var publisher = mock(TransactionalApplicationEventPublisher.class);
        var reporter = new ModulithErrorReporter(publisher);
        var oversized = "\u0000".repeat(10_000);

        reporter.report(
                ErrorEvent.builder()
                        .sourceType(ErrorSource.WEB)
                        .sourceName(oversized)
                        .requestParams(oversized)
                        .message(oversized)
                        .stackTrace(oversized)
                        .build());

        var event = ArgumentCaptor.forClass(ErrorEvent.class);
        verify(publisher).publishEventRequiresNew(event.capture());
        assertThat(JsonUtil.toJsonString(event.getValue())).hasSizeLessThanOrEqualTo(3500);
    }
}
