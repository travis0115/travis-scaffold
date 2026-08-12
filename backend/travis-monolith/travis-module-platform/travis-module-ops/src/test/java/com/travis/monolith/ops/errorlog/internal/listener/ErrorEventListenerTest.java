package com.travis.monolith.ops.errorlog.internal.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.travis.infrastructure.common.monitor.error.ErrorEvent;
import com.travis.infrastructure.common.monitor.error.ErrorSource;
import com.travis.monolith.ops.errorlog.internal.entity.SysErrorLog;
import com.travis.monolith.ops.errorlog.internal.service.SysErrorLogService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ErrorEventListenerTest {

    @Test
    void shouldPersistCompleteErrorSnapshot() {
        var service = mock(SysErrorLogService.class);
        var listener = new SystemErrorEventListener(service);
        var event =
                ErrorEvent.builder()
                          .sourceType(ErrorSource.ROCKETMQ)
                          .sourceName("ExampleConsumer#consume")
                          .businessKey("message-1")
                          .requestId("request-1")
                          .traceId("trace-1")
                          .requestParams("{\"id\":1}")
                          .exceptionClass(IllegalStateException.class.getName())
                          .message("failed")
                          .stackTrace("stack")
                          .build();

        listener.handle(event);

        var entity = ArgumentCaptor.forClass(SysErrorLog.class);
        verify(service).save(entity.capture());
        assertThat(entity.getValue().getSourceType()).isEqualTo("ROCKETMQ");
        assertThat(entity.getValue().getSourceName()).isEqualTo("ExampleConsumer#consume");
        assertThat(entity.getValue().getBusinessKey()).isEqualTo("message-1");
        assertThat(entity.getValue().getRequestId()).isEqualTo("request-1");
        assertThat(entity.getValue().getTraceId()).isEqualTo("trace-1");
        assertThat(entity.getValue().getRequestParams()).contains("id");
    }
}
