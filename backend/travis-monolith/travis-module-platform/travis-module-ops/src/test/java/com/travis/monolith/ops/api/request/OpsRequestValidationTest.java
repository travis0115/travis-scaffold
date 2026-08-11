package com.travis.monolith.ops.api.request;

import static org.assertj.core.api.Assertions.assertThat;

import com.travis.monolith.ops.errorlog.api.request.SysErrorLogPageReq;
import com.travis.monolith.ops.job.api.request.OpsJobCreateReq;
import com.travis.monolith.ops.job.api.request.OpsJobLogPageReq;
import jakarta.validation.Validation;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class OpsRequestValidationTest {

    @Test
    void shouldRejectInvalidJobFields() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var request = new OpsJobCreateReq();
            request.setJobName("job");
            request.setHandlerName("handler");
            request.setScheduleType("UNKNOWN");
            request.setConcurrent(2);
            request.setMisfirePolicy(9);
            request.setIntervalMillis(0L);
            request.setExcludedWeekdays(List.of(0, 8));
            request.setAlertUserIds(List.of(-1L));
            request.setOwnerUserId(0L);

            var messages =
                    factory.getValidator().validate(request).stream()
                            .map(violation -> violation.getMessage())
                            .toList();

            assertThat(messages)
                    .contains(
                            "调度类型错误",
                            "并发策略值错误",
                            "错过执行策略值错误",
                            "执行间隔必须为正数",
                            "星期值不能小于1",
                            "星期值不能大于7",
                            "告警用户ID必须为正数",
                            "负责人用户ID必须为正数");
        }
    }

    @Test
    void shouldRejectReversedQueryTimeRanges() {
        var start = LocalDateTime.of(2026, 8, 11, 12, 0);
        var end = start.minusMinutes(1);
        var jobLogRequest = new OpsJobLogPageReq();
        jobLogRequest.setStartTime(start);
        jobLogRequest.setEndTime(end);
        var errorLogRequest = new SysErrorLogPageReq();
        errorLogRequest.setStartTime(start);
        errorLogRequest.setEndTime(end);

        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertThat(validator.validate(jobLogRequest))
                    .anyMatch(violation -> "开始时间不能晚于结束时间".equals(violation.getMessage()));
            assertThat(validator.validate(errorLogRequest))
                    .anyMatch(violation -> "开始时间不能晚于结束时间".equals(violation.getMessage()));
        }
    }
}
