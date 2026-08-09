package com.travis.monolith.system.message.internal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.framework.web.core.xss.HtmlSanitizer;
import com.travis.monolith.system.message.api.enums.SysMessageChannel;
import com.travis.monolith.system.message.api.enums.SysMessagePushType;
import com.travis.monolith.system.message.api.enums.SysMessageReceiverScope;
import com.travis.monolith.system.message.api.request.SysMessageCreateReq;
import com.travis.monolith.system.message.internal.service.SysMessageService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SysMessageApiImplTest {

    @Test
    void shouldPublishToSpecifiedReceiverType() {
        SysMessageService messageService = mock(SysMessageService.class);
        HtmlSanitizer htmlSanitizer = mock(HtmlSanitizer.class);
        var api = new SysMessageApiImpl(messageService, htmlSanitizer);
        when(htmlSanitizer.sanitize("<p>注册成功</p>")).thenReturn("<p>注册成功</p>");
        when(messageService.createSystem(any())).thenReturn(100L);

        api.publishToUsers(LoginType.APP, "注册成功", "<p>注册成功</p>", List.of(200L));

        var captor = ArgumentCaptor.forClass(SysMessageCreateReq.class);
        verify(messageService).createSystem(captor.capture());
        verify(messageService).pushAutomatic(100L);
        SysMessageCreateReq request = captor.getValue();
        assertThat(request.getReceiverType()).isEqualTo(LoginType.APP);
        assertThat(request.getReceiverScope()).isEqualTo(SysMessageReceiverScope.USER.getValue());
        assertThat(request.getReceiverValues()).containsExactly(200L);
        assertThat(request.getPushType()).isEqualTo(SysMessagePushType.MANUAL.getValue());
        assertThat(request.getChannel()).isEqualTo(SysMessageChannel.IN_APP.getValue());
    }

    @Test
    void shouldAllowImageOnlyContent() {
        SysMessageService messageService = mock(SysMessageService.class);
        HtmlSanitizer htmlSanitizer = mock(HtmlSanitizer.class);
        var api = new SysMessageApiImpl(messageService, htmlSanitizer);
        var content = "<img src=\"https://example.com/image.png\">";
        when(htmlSanitizer.sanitize(content)).thenReturn(content);
        when(messageService.createSystem(any())).thenReturn(100L);

        api.publishToUsers(LoginType.APP, "图片消息", content, List.of(200L));

        verify(messageService).pushAutomatic(100L);
    }
}
