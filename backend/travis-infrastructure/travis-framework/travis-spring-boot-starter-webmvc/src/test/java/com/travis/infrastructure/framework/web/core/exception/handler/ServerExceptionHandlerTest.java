package com.travis.infrastructure.framework.web.core.exception.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.travis.infrastructure.common.monitor.error.ErrorEvent;
import com.travis.infrastructure.common.monitor.error.ErrorReporter;
import com.travis.infrastructure.common.monitor.error.ErrorSource;
import com.travis.infrastructure.common.web.constant.MdcKey;
import com.travis.infrastructure.framework.web.core.util.ErrorRequestSnapshotter;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;

class ServerExceptionHandlerTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void shouldReportUnexpectedWebExceptionWithRequestSnapshot() {
        var reporter = mock(ErrorReporter.class);
        var snapshotter = mock(ErrorRequestSnapshotter.class);
        var request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/admin/test/1");
        when(request.getMethod()).thenReturn("POST");
        when(snapshotter.snapshot(request))
                .thenReturn(
                        new ErrorRequestSnapshotter.Snapshot(
                                "TestController#create", "{\"id\":\"1\"}"));
        MDC.put(MdcKey.REQUEST_ID, "request-1");
        MDC.put(MdcKey.TRACE_ID, "trace-1");
        MDC.put(MdcKey.USER_ID, "7");
        var handler = new ServerExceptionHandler(reporter, snapshotter);

        handler.handleException(new NullPointerException("unexpected"), request);

        var event = ArgumentCaptor.forClass(ErrorEvent.class);
        verify(reporter).report(event.capture());
        assertThat(event.getValue().sourceType()).isEqualTo(ErrorSource.WEB);
        assertThat(event.getValue().sourceName()).isEqualTo("TestController#create");
        assertThat(event.getValue().requestId()).isEqualTo("request-1");
        assertThat(event.getValue().traceId()).isEqualTo("trace-1");
        assertThat(event.getValue().userId()).isEqualTo(7L);
        assertThat(event.getValue().requestParams()).contains("id");
        assertThat(event.getValue().stackTrace()).contains("NullPointerException");
    }
}
