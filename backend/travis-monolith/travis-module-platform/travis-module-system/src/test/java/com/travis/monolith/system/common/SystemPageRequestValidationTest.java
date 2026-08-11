package com.travis.monolith.system.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.travis.monolith.system.log.loginlog.api.request.SysLoginLogPageReq;
import com.travis.monolith.system.log.operationlog.api.request.SysOperationLogPageReq;
import com.travis.monolith.system.notice.api.request.SysNoticePageReq;
import com.travis.monolith.system.version.api.request.SysVersionPageReq;
import jakarta.validation.Validation;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class SystemPageRequestValidationTest {

    @Test
    void shouldRejectReversedPublishDateRanges() {
        var start = LocalDate.of(2026, 8, 11);
        var end = start.minusDays(1);
        var noticeRequest = new SysNoticePageReq();
        noticeRequest.setPublishStartDate(start);
        noticeRequest.setPublishEndDate(end);
        var versionRequest = new SysVersionPageReq();
        versionRequest.setPublishStartDate(start);
        versionRequest.setPublishEndDate(end);

        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertThat(validator.validate(noticeRequest))
                    .anyMatch(violation -> "发布开始日期不能晚于结束日期".equals(violation.getMessage()));
            assertThat(validator.validate(versionRequest))
                    .anyMatch(violation -> "发布开始日期不能晚于结束日期".equals(violation.getMessage()));
        }
    }

    @Test
    void shouldRejectReversedLogTimeRanges() {
        var start = LocalDateTime.of(2026, 8, 11, 12, 0);
        var end = start.minusMinutes(1);
        var loginLogRequest = new SysLoginLogPageReq();
        loginLogRequest.setStartTime(start);
        loginLogRequest.setEndTime(end);
        var operationLogRequest = new SysOperationLogPageReq();
        operationLogRequest.setStartTime(start);
        operationLogRequest.setEndTime(end);

        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertThat(validator.validate(loginLogRequest))
                    .anyMatch(violation -> "开始时间不能晚于结束时间".equals(violation.getMessage()));
            assertThat(validator.validate(operationLogRequest))
                    .anyMatch(violation -> "开始时间不能晚于结束时间".equals(violation.getMessage()));
        }
    }
}
