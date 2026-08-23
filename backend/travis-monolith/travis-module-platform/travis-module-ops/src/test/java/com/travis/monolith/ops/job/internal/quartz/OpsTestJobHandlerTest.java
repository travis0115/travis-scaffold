package com.travis.monolith.ops.job.internal.quartz;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.travis.infrastructure.framework.quartz.core.QuartzJobHandlerRegistry;
import java.util.List;
import org.junit.jupiter.api.Test;

class OpsTestJobHandlerTest {

    @Test
    void shouldRegisterAndExecuteTestHandler() {
        var handler = new OpsTestJobHandler();
        var registry = new QuartzJobHandlerRegistry(List.of(handler));

        assertThat(registry.contains("opsTestJob")).isTrue();
        assertThat(registry.names()).containsExactly("opsTestJob");
        assertThatCode(() -> registry.getRequired("opsTestJob").execute("{}"))
                .doesNotThrowAnyException();
    }
}
