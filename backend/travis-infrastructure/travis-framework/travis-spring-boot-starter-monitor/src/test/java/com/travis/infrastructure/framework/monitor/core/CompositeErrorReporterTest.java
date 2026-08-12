package com.travis.infrastructure.framework.monitor.core;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.travis.infrastructure.common.monitor.error.ErrorEvent;
import com.travis.infrastructure.common.monitor.error.ErrorSource;
import java.util.List;
import org.junit.jupiter.api.Test;

class CompositeErrorReporterTest {

    @Test
    void shouldReportToAllContributorsInOrder() {
        var first = mock(ErrorReporterContributor.class);
        var second = mock(ErrorReporterContributor.class);
        var reporter = new CompositeErrorReporter(List.of(first, second));
        var event = ErrorEvent.builder().sourceType(ErrorSource.WEB).build();

        reporter.report(event);

        var ordered = inOrder(first, second);
        ordered.verify(first).report(event);
        ordered.verify(second).report(event);
    }

    @Test
    void shouldContinueWhenOneContributorFails() {
        var failed = mock(ErrorReporterContributor.class);
        var succeeding = mock(ErrorReporterContributor.class);
        var reporter = new CompositeErrorReporter(List.of(failed, succeeding));
        var event = ErrorEvent.builder().sourceType(ErrorSource.ASYNC).build();
        doThrow(new IllegalStateException("failed")).when(failed).report(event);

        reporter.report(event);

        verify(succeeding).report(event);
    }
}
