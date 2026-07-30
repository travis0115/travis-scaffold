package com.travis.monolith.system.message.internal.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.travis.infrastructure.framework.websocket.core.sender.WebSocketMessageSender;
import com.travis.monolith.system.file.api.SysFileApi;
import com.travis.monolith.system.message.api.enums.SysMessageChannel;
import com.travis.monolith.system.message.api.enums.SysMessagePushType;
import com.travis.monolith.system.message.api.enums.SysMessageReceiverScope;
import com.travis.monolith.system.message.api.enums.SysMessageSourceType;
import com.travis.monolith.system.message.api.enums.SysMessageStatus;
import com.travis.monolith.system.message.internal.channel.SysMessageChannelHandler;
import com.travis.monolith.system.message.internal.channel.SysMessageChannelHandlerRegistry;
import com.travis.monolith.system.message.internal.converter.SysMessageConverter;
import com.travis.monolith.system.message.internal.entity.SysMessage;
import com.travis.monolith.system.message.internal.mapper.SysMessageMapper;
import com.travis.monolith.system.message.internal.quartz.SysMessageScheduledPushScheduler;
import com.travis.monolith.system.message.internal.service.SysMessageReceiverService;
import com.travis.monolith.system.message.internal.service.SysMessageTemplateService;
import com.travis.monolith.system.role.api.SysRoleApi;
import com.travis.monolith.system.user.api.SysUserApi;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.test.util.ReflectionTestUtils;

class SysMessageServiceImplTest {

    @Test
    void shouldEscapeTemplateParametersInsertedIntoInAppRichText() {
        String rendered =
                SysMessageServiceImpl.renderTemplate(
                        "<p>你好，{{name}}</p>", Map.of("name", "<img src=x onerror=alert(1)>"), true);

        assertThat(rendered).isEqualTo("<p>你好，&lt;img src=x onerror=alert(1)&gt;</p>");
    }

    @Test
    void shouldKeepTemplateParametersUnescapedForPlainFields() {
        String rendered =
                SysMessageServiceImpl.renderTemplate("/user/{{id}}", Map.of("id", 1001), false);

        assertThat(rendered).isEqualTo("/user/1001");
    }

    @Test
    void shouldNotSendWhenAnotherPublisherHasClaimedTheMessage() {
        SysMessageMapper mapper = mock(SysMessageMapper.class);
        SysMessageChannelHandler handler = mock(SysMessageChannelHandler.class);
        SysMessageChannelHandlerRegistry registry = mock(SysMessageChannelHandlerRegistry.class);
        var service =
                new SysMessageServiceImpl(
                        mock(SysMessageReceiverService.class),
                        mock(SysMessageTemplateService.class),
                        mock(SysMessageConverter.class),
                        mock(WebSocketMessageSender.class),
                        mock(SysFileApi.class),
                        mock(CacheManager.class),
                        registry,
                        mock(SysMessageScheduledPushScheduler.class),
                        mock(SysUserApi.class),
                        mock(SysRoleApi.class));
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        var message = new SysMessage();
        message.setId(1001L);
        message.setSourceType(SysMessageSourceType.MANUAL.getValue());
        message.setChannel(SysMessageChannel.IN_APP.getValue());
        message.setPushType(SysMessagePushType.MANUAL.getValue());
        message.setStatus(SysMessageStatus.PENDING.getValue());
        message.setReceiverType("admin");
        message.setReceiverScope(SysMessageReceiverScope.ALL.getValue());
        when(mapper.selectById(1001L)).thenReturn(message);
        when(mapper.claimForPublish(any(), any(), any(), any(), any())).thenReturn(0);
        when(registry.get(SysMessageChannel.IN_APP.getValue())).thenReturn(handler);

        service.push(1001L);

        verify(handler, never()).send(any());
    }
}
