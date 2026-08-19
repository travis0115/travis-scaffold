package com.travis.monolith.ops.errorlog.internal.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.travis.infrastructure.common.monitor.error.ErrorEvent;
import com.travis.infrastructure.common.monitor.error.ErrorSource;
import com.travis.monolith.ops.errorlog.internal.entity.SysErrorLog;
import com.travis.monolith.ops.errorlog.internal.entity.SysErrorLogOccurrence;
import com.travis.monolith.ops.errorlog.internal.service.SysErrorLogService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;

class ErrorEventListenerTest {

    @Test
    void shouldPersistCompleteErrorSnapshot() {
        var service = mock(SysErrorLogService.class);
        var environment = mock(Environment.class);
        var buildProperties = mock(BuildProperties.class);
        when(environment.getProperty("spring.application.name")).thenReturn("travis-monolith");
        when(buildProperties.getVersion()).thenReturn("1.0.0");
        when(environment.getProperty("HOSTNAME", "local")).thenReturn("node-1");
        when(environment.getProperty("spring.application.instance-id", "node-1"))
                .thenReturn("node-1");
        var listener =
                new SystemErrorEventListener(service, environment, Optional.of(buildProperties));
        var event =
                ErrorEvent.builder()
                        .sourceType(ErrorSource.ROCKETMQ)
                        .sourceName("ExampleConsumer#consume")
                        .businessKey("message-1")
                        .userId(7L)
                        .username("app-user")
                        .requestId("request-1")
                        .traceId("trace-1")
                        .requestParams("{\"id\":1}")
                        .exceptionClass(IllegalStateException.class.getName())
                        .message("failed")
                        .stackTrace("stack")
                        .build();

        listener.handle(event);

        var entity = ArgumentCaptor.forClass(SysErrorLog.class);
        var occurrence = ArgumentCaptor.forClass(SysErrorLogOccurrence.class);
        verify(service).record(entity.capture(), occurrence.capture());
        assertThat(entity.getValue().getSourceType()).isEqualTo("ROCKETMQ");
        assertThat(entity.getValue().getSourceName()).isEqualTo("ExampleConsumer#consume");
        assertThat(entity.getValue().getBusinessKey()).isEqualTo("message-1");
        assertThat(entity.getValue().getUsername()).isEqualTo("app-user");
        assertThat(entity.getValue().getRequestId()).isEqualTo("request-1");
        assertThat(entity.getValue().getTraceId()).isEqualTo("trace-1");
        assertThat(entity.getValue().getRequestParams()).contains("id");
        assertThat(entity.getValue().getPlatformType()).isEqualTo("SYSTEM");
        assertThat(entity.getValue().getOccurrenceCount()).isEqualTo(1);
        assertThat(entity.getValue().getApplicationVersion()).isEqualTo("1.0.0");
        assertThat(occurrence.getValue().getStackTrace()).isEqualTo("stack");
        assertThat(occurrence.getValue().getApplicationVersion()).isEqualTo("1.0.0");
        assertThat(occurrence.getValue().getUsername()).isEqualTo("app-user");
    }
}
