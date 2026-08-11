package com.travis.monolith.system.message.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.framework.quartz.core.QuartzSyncExecutor;
import com.travis.monolith.system.dept.api.SysDeptApi;
import com.travis.monolith.system.file.api.SysFileApi;
import com.travis.monolith.system.message.api.SysMessageReceiverTargetValidator;
import com.travis.monolith.system.message.api.enums.SysMessageChannel;
import com.travis.monolith.system.message.api.enums.SysMessagePushType;
import com.travis.monolith.system.message.api.enums.SysMessageReceiverScope;
import com.travis.monolith.system.message.api.enums.SysMessageSourceType;
import com.travis.monolith.system.message.api.enums.SysMessageStatus;
import com.travis.monolith.system.message.api.request.SysMessageUpdateReq;
import com.travis.monolith.system.message.internal.channel.SysMessageChannelHandler;
import com.travis.monolith.system.message.internal.channel.SysMessageChannelHandlerRegistry;
import com.travis.monolith.system.message.internal.converter.SysMessageConverter;
import com.travis.monolith.system.message.internal.entity.SysMessage;
import com.travis.monolith.system.message.internal.mapper.SysMessageMapper;
import com.travis.monolith.system.message.internal.notification.SysMessageInboxNotifier;
import com.travis.monolith.system.message.internal.quartz.SysMessageScheduledPushScheduler;
import com.travis.monolith.system.message.internal.service.impl.SysMessageServiceImpl;
import com.travis.monolith.system.role.api.SysRoleApi;
import com.travis.monolith.system.user.api.SysUserApi;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;

class SysMessageServiceImplTest {

    @Test
    void shouldRestoreRevokedMessageToPendingWhenChangedToScheduledPush() {
        var mapper = mock(SysMessageMapper.class);
        var converter = mock(SysMessageConverter.class);
        var channelRegistry = mock(SysMessageChannelHandlerRegistry.class);
        var fileApi = mock(SysFileApi.class);
        var quartzSyncExecutor = mock(QuartzSyncExecutor.class);
        var receiverService = mock(SysMessageReceiverService.class);
        var message = revokedMessage();
        var request = scheduledUpdateRequest();
        when(mapper.selectByIdForUpdate(message.getId())).thenReturn(message);
        when(channelRegistry.get(SysMessageChannel.IN_APP.getValue()))
                .thenReturn(mock(SysMessageChannelHandler.class));
        when(fileApi.stripManagedImageSources(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doAnswer(
                        invocation -> {
                            var req = invocation.getArgument(0, SysMessageUpdateReq.class);
                            var entity = invocation.getArgument(1, SysMessage.class);
                            entity.setTitle(req.getTitle());
                            entity.setContent(req.getContent());
                            entity.setPushType(req.getPushType());
                            entity.setPublishTime(req.getPublishTime());
                            entity.setChannel(req.getChannel());
                            entity.setReceiverType(req.getReceiverType());
                            entity.setReceiverScope(req.getReceiverScope());
                            return null;
                        })
                .when(converter)
                .update(request, message);
        var service =
                service(
                        mapper,
                        converter,
                        channelRegistry,
                        fileApi,
                        quartzSyncExecutor,
                        receiverService);

        service.update(message.getId(), request);

        assertThat(message.getStatus()).isEqualTo(SysMessageStatus.PENDING.getValue());
        verify(mapper).updateById(message);
        verify(receiverService, never()).resetReadStatus(message.getId());
        verify(quartzSyncExecutor).executeAfterCommit(any(), any());
    }

    @Test
    void shouldResetReadStatusOnlyAfterScheduledMessageIsPublished() {
        var mapper = mock(SysMessageMapper.class);
        var channelRegistry = mock(SysMessageChannelHandlerRegistry.class);
        var receiverService = mock(SysMessageReceiverService.class);
        var message = scheduledMessageDueForPublish();
        when(mapper.selectByIdForUpdate(message.getId())).thenReturn(message);
        when(mapper.claimForPublish(any(), any(), any(), any(), any(), any(), any())).thenReturn(1);
        when(channelRegistry.get(SysMessageChannel.IN_APP.getValue()))
                .thenReturn(mock(SysMessageChannelHandler.class));
        var service =
                service(
                        mapper,
                        mock(SysMessageConverter.class),
                        channelRegistry,
                        mock(SysFileApi.class),
                        mock(QuartzSyncExecutor.class),
                        receiverService);

        assertThat(service.pushScheduled(message.getId())).isTrue();

        verify(receiverService).resetReadStatus(message.getId());
    }

    @Test
    void shouldRejectManualPushForScheduledMessage() {
        var mapper = mock(SysMessageMapper.class);
        var message = revokedMessage();
        message.setPushType(SysMessagePushType.SCHEDULED.getValue());
        when(mapper.selectByIdForUpdate(message.getId())).thenReturn(message);
        var service =
                service(
                        mapper,
                        mock(SysMessageConverter.class),
                        mock(SysMessageChannelHandlerRegistry.class),
                        mock(SysFileApi.class),
                        mock(QuartzSyncExecutor.class),
                        mock(SysMessageReceiverService.class));

        assertThatThrownBy(() -> service.push(message.getId())).hasMessageContaining("定时消息不支持手动推送");
        verify(mapper, never()).claimForPublish(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldRejectDeletingSentMessage() {
        var mapper = mock(SysMessageMapper.class);
        var receiverService = mock(SysMessageReceiverService.class);
        var message = revokedMessage();
        message.setStatus(SysMessageStatus.SENT.getValue());
        when(mapper.selectByIdForUpdate(message.getId())).thenReturn(message);
        var service =
                service(
                        mapper,
                        mock(SysMessageConverter.class),
                        mock(SysMessageChannelHandlerRegistry.class),
                        mock(SysFileApi.class),
                        mock(QuartzSyncExecutor.class),
                        receiverService);

        assertThatThrownBy(() -> service.delete(message.getId())).hasMessageContaining("已推送消息请先撤回");
        verify(receiverService, never()).deleteByMessageId(message.getId());
        verify(mapper, never()).deleteById(message.getId());
    }

    private SysMessageServiceImpl service(
            SysMessageMapper mapper,
            SysMessageConverter converter,
            SysMessageChannelHandlerRegistry channelRegistry,
            SysFileApi fileApi,
            QuartzSyncExecutor quartzSyncExecutor,
            SysMessageReceiverService receiverService) {
        @SuppressWarnings("unchecked")
        ObjectProvider<SysMessageReceiverTargetValidator> validators = mock(ObjectProvider.class);
        var service =
                new SysMessageServiceImpl(
                        receiverService,
                        mock(SysMessageTemplateService.class),
                        converter,
                        mock(SysMessageInboxNotifier.class),
                        fileApi,
                        channelRegistry,
                        mock(SysMessageScheduledPushScheduler.class),
                        quartzSyncExecutor,
                        mock(SysUserApi.class),
                        mock(SysRoleApi.class),
                        mock(SysDeptApi.class),
                        validators);
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        return service;
    }

    private SysMessage revokedMessage() {
        var message = new SysMessage();
        message.setId(100L);
        message.setSourceType(SysMessageSourceType.MANUAL.getValue());
        message.setStatus(SysMessageStatus.REVOKED.getValue());
        return message;
    }

    private SysMessageUpdateReq scheduledUpdateRequest() {
        var request = new SysMessageUpdateReq();
        request.setTitle("定时消息");
        request.setContent("<p>定时消息内容</p>");
        request.setPushType(SysMessagePushType.SCHEDULED.getValue());
        request.setPublishTime(LocalDateTime.now().plusHours(1));
        request.setChannel(SysMessageChannel.IN_APP.getValue());
        request.setReceiverType(LoginType.ADMIN);
        request.setReceiverScope(SysMessageReceiverScope.ALL.getValue());
        return request;
    }

    private SysMessage scheduledMessageDueForPublish() {
        var message = new SysMessage();
        message.setId(100L);
        message.setSourceType(SysMessageSourceType.MANUAL.getValue());
        message.setStatus(SysMessageStatus.PENDING.getValue());
        message.setPushType(SysMessagePushType.SCHEDULED.getValue());
        message.setPublishTime(LocalDateTime.now().minusMinutes(1));
        message.setChannel(SysMessageChannel.IN_APP.getValue());
        message.setReceiverType(LoginType.ADMIN);
        message.setReceiverScope(SysMessageReceiverScope.ALL.getValue());
        return message;
    }
}
