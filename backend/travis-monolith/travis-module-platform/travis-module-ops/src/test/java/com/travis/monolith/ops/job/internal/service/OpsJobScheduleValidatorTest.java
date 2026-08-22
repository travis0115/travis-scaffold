package com.travis.monolith.ops.job.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.monolith.ops.job.internal.entity.OpsJob;
import org.junit.jupiter.api.Test;

class OpsJobScheduleValidatorTest {

    @Test
    void shouldRejectInvalidCronExpression() {
        var job = new OpsJob();
        job.setScheduleType("CRON");
        job.setCronExpression("invalid");

        assertThatThrownBy(() -> OpsJobScheduleValidator.validate(job))
                .isInstanceOfSatisfying(
                        BizException.class,
                        exception ->
                                assertThat(exception.getArgs()).containsExactly("Cron 表达式不合法"));
    }

    @Test
    void shouldAcceptValidIntervalSchedule() {
        var job = new OpsJob();
        job.setScheduleType("INTERVAL");
        job.setIntervalMillis(1000L);

        assertThatCode(() -> OpsJobScheduleValidator.validate(job)).doesNotThrowAnyException();
    }
}
