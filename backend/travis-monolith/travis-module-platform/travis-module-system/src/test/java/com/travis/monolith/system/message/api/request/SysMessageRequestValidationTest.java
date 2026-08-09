package com.travis.monolith.system.message.api.request;

import static org.assertj.core.api.Assertions.assertThat;

import com.travis.monolith.system.message.api.enums.SysMessagePushType;
import com.travis.monolith.system.message.api.enums.SysMessageSourceType;
import com.travis.monolith.system.message.api.enums.SysMessageType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class SysMessageRequestValidationTest {

    @Test
    void shouldAllowImageOnlyContentAndRejectEmptyMarkup() {
        var request = new SysMessageCreateReq();
        request.setContent("<img src=\"https://example.com/image.png\">");
        assertThat(request.isContentValid()).isTrue();

        request.setContent("<p><br></p>");
        assertThat(request.isContentValid()).isFalse();

        var template = new SysMessageTemplateCreateReq();
        template.setContent("<p><img src=\"https://example.com/image.png\"></p>");
        assertThat(template.isContentValid()).isTrue();

        template.setContent("<p>&nbsp;</p>");
        assertThat(template.isContentValid()).isFalse();
    }

    @Test
    void shouldRestrictManualPushTypeAndScheduledTime() {
        var request = new SysMessageCreateReq();
        request.setPushType(SysMessagePushType.AUTO.getValue());
        assertThat(request.isManualPushTypeValid()).isFalse();

        request.setPushType(SysMessagePushType.SCHEDULED.getValue());
        request.setPublishTime(LocalDateTime.now().minusMinutes(1));
        assertThat(request.isScheduledPublishTimeValid()).isFalse();

        request.setPublishTime(LocalDateTime.now().plusMinutes(1));
        assertThat(request.isScheduledPublishTimeValid()).isTrue();
    }

    @Test
    void shouldMatchMessageTypeWithBusinessSource() {
        var request = new SysSourceMessagePublishReq();
        request.setSourceType(SysMessageSourceType.VERSION.getValue());
        request.setMessageType(SysMessageType.VERSION.getValue());
        assertThat(request.isMessageSourceTypeValid()).isTrue();

        request.setMessageType(SysMessageType.NOTICE.getValue());
        assertThat(request.isMessageSourceTypeValid()).isFalse();

        request.setSourceType(SysMessageSourceType.SYSTEM.getValue());
        assertThat(request.isMessageSourceTypeValid()).isFalse();
    }

    @Test
    void shouldRejectReversedPublishDateRange() {
        var request = new SysMessagePageReq();
        request.setPublishStartDate(LocalDate.of(2026, 8, 10));
        request.setPublishEndDate(LocalDate.of(2026, 8, 9));
        assertThat(request.isPublishDateRangeValid()).isFalse();
    }
}
