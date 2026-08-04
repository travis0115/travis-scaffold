package com.travis.monolith.system.message.internal.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.redis.core.annotation.DistributedLock;
import com.travis.infrastructure.framework.websocket.core.sender.WebSocketMessageSender;
import com.travis.monolith.system.dept.api.SysDeptApi;
import com.travis.monolith.system.file.api.SysFileApi;
import com.travis.monolith.system.message.api.enums.SysMessageChannel;
import com.travis.monolith.system.message.api.enums.SysMessagePushType;
import com.travis.monolith.system.message.api.enums.SysMessageReceiverScope;
import com.travis.monolith.system.message.api.enums.SysMessageSourceType;
import com.travis.monolith.system.message.api.enums.SysMessageStatus;
import com.travis.monolith.system.message.api.request.SysMessagePageReq;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class SysMessageServiceImplTest {

    @Test
    @SuppressWarnings("unchecked")
    void shouldApplyPublishDateRangeToAdminPageQuery() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), "message-page-test"),
                SysMessage.class);
        SysMessageMapper mapper = mock(SysMessageMapper.class);
        var service = newService(mapper, mock(SysMessageChannelHandlerRegistry.class));
        var req = new SysMessagePageReq();
        req.setPublishStartDate(LocalDate.of(2026, 8, 1));
        req.setPublishEndDate(LocalDate.of(2026, 8, 3));
        when(mapper.page(eq(1), eq(10), any(LambdaQueryWrapperX.class)))
                .thenReturn(new Page<SysMessage>());

        service.page(req);

        ArgumentCaptor<LambdaQueryWrapperX<SysMessage>> wrapperCaptor =
                (ArgumentCaptor<LambdaQueryWrapperX<SysMessage>>)
                        (ArgumentCaptor<?>) ArgumentCaptor.forClass(LambdaQueryWrapperX.class);
        verify(mapper).page(eq(1), eq(10), wrapperCaptor.capture());
        var wrapper = wrapperCaptor.getValue();
        assertThat(wrapper.getSqlSegment()).contains("publish_time >=", "publish_time <");
        assertThat(wrapper.getParamNameValuePairs().values())
                .contains(
                        LocalDate.of(2026, 8, 1).atStartOfDay(),
                        LocalDate.of(2026, 8, 4).atStartOfDay());
    }

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
                        registry,
                        mock(SysMessageScheduledPushScheduler.class),
                        mock(SysUserApi.class),
                        mock(SysRoleApi.class),
                        mock(SysDeptApi.class));
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        var message = new SysMessage();
        message.setId(1001L);
        message.setSourceType(SysMessageSourceType.MANUAL.getValue());
        message.setChannel(SysMessageChannel.IN_APP.getValue());
        message.setPushType(SysMessagePushType.MANUAL.getValue());
        message.setStatus(SysMessageStatus.PENDING.getValue());
        message.setReceiverType("admin");
        message.setReceiverScope(SysMessageReceiverScope.ALL.getValue());
        when(mapper.selectByIdForUpdate(1001L)).thenReturn(message);
        when(mapper.claimForPublish(any(), any(), any(), any(), any(), any(), any())).thenReturn(0);
        when(registry.get(SysMessageChannel.IN_APP.getValue())).thenReturn(handler);

        service.push(1001L);

        verify(handler, never()).send(any());
    }

    @Test
    void shouldClaimExpectedStateAndNotifyReceiverNamespaceAfterPublish() {
        SysMessageMapper mapper = mock(SysMessageMapper.class);
        SysMessageChannelHandler handler = mock(SysMessageChannelHandler.class);
        SysMessageChannelHandlerRegistry registry = mock(SysMessageChannelHandlerRegistry.class);
        WebSocketMessageSender sender = mock(WebSocketMessageSender.class);
        var service =
                new SysMessageServiceImpl(
                        mock(SysMessageReceiverService.class),
                        mock(SysMessageTemplateService.class),
                        mock(SysMessageConverter.class),
                        sender,
                        mock(SysFileApi.class),
                        registry,
                        mock(SysMessageScheduledPushScheduler.class),
                        mock(SysUserApi.class),
                        mock(SysRoleApi.class),
                        mock(SysDeptApi.class));
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        var message = new SysMessage();
        message.setId(1001L);
        message.setSourceType(SysMessageSourceType.MANUAL.getValue());
        message.setChannel(SysMessageChannel.IN_APP.getValue());
        message.setPushType(SysMessagePushType.MANUAL.getValue());
        message.setStatus(SysMessageStatus.PENDING.getValue());
        message.setReceiverType("admin");
        message.setReceiverScope(SysMessageReceiverScope.ALL.getValue());
        when(mapper.selectByIdForUpdate(1001L)).thenReturn(message);
        when(mapper.claimForPublish(any(), any(), any(), any(), any(), any(), any())).thenReturn(1);
        when(registry.get(SysMessageChannel.IN_APP.getValue())).thenReturn(handler);

        service.push(1001L);

        verify(mapper)
                .claimForPublish(
                        eq(1001L),
                        eq(SysMessageStatus.PENDING.getValue()),
                        eq(SysMessagePushType.MANUAL.getValue()),
                        isNull(),
                        eq(SysMessagePushType.MANUAL.getValue()),
                        eq(SysMessageStatus.SENT.getValue()),
                        any(LocalDateTime.class));
        verify(handler).send(message);
        verify(sender).sendToNamespace(eq("admin"), any());
    }

    @Test
    void shouldRejectPastScheduledPublishTime() {
        var service =
                newService(
                        mock(SysMessageMapper.class), mock(SysMessageChannelHandlerRegistry.class));

        assertThatThrownBy(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        service,
                                        "validatePush",
                                        SysMessagePushType.SCHEDULED.getValue(),
                                        LocalDateTime.now().minusMinutes(1)))
                .isInstanceOf(BizException.class)
                .satisfies(
                        exception ->
                                assertThat(((BizException) exception).getArgs())
                                        .contains("定时发布时间必须晚于当前时间"));
    }

    @Test
    void shouldRejectReceiverListOverLimitBeforeDeduplication() {
        var service =
                newService(
                        mock(SysMessageMapper.class), mock(SysMessageChannelHandlerRegistry.class));
        List<Long> receiverValues = Collections.nCopies(1001, 1L);

        assertThatThrownBy(
                        () ->
                                ReflectionTestUtils.invokeMethod(
                                        service,
                                        "validateReceiver",
                                        "app",
                                        SysMessageReceiverScope.USER.getValue(),
                                        receiverValues))
                .isInstanceOf(BizException.class)
                .satisfies(
                        exception ->
                                assertThat(((BizException) exception).getArgs())
                                        .contains("接收对象ID不合法或数量超过1000个"));
    }

    @Test
    void shouldRejectRenderedFieldsThatExceedDatabaseLimits() {
        var message = new SysMessage();
        message.setTitle("a".repeat(256));

        assertThatThrownBy(() -> SysMessageServiceImpl.validateRenderedFields(message))
                .isInstanceOf(BizException.class)
                .satisfies(
                        exception ->
                                assertThat(((BizException) exception).getArgs())
                                        .contains("消息标题渲染后长度不能超过255个字符"));
    }

    @Test
    void shouldRejectBusinessSourceMessageFromManualDetailQuery() {
        SysMessageMapper mapper = mock(SysMessageMapper.class);
        var service = newService(mapper, mock(SysMessageChannelHandlerRegistry.class));
        var message = new SysMessage();
        message.setSourceType(SysMessageSourceType.NOTICE.getValue());
        when(mapper.selectById(1001L)).thenReturn(message);

        assertThatThrownBy(() -> service.getOrThrow(1001L))
                .isInstanceOf(BizException.class)
                .satisfies(
                        exception ->
                                assertThat(((BizException) exception).getArgs())
                                        .contains("来源消息请在对应业务中操作"));
    }

    @Test
    void shouldUseActualLogicalDeleteColumnInPublishClaimSql() throws Exception {
        try (var input = getClass().getResourceAsStream("/mapper/SysMessageMapper.xml")) {
            assertThat(input).isNotNull();
            String mapperXml = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(mapperXml).contains("AND is_deleted = 0");
            assertThat(mapperXml).doesNotContain("AND deleted = 0");
        }
    }

    @Test
    void shouldSerializeSourceMessagePublicationWithDistributedLock() throws Exception {
        var publishMethod =
                SysMessageServiceImpl.class.getMethod(
                        "publishSourceMessage",
                        com.travis.monolith.system.message.api.request.SysSourceMessagePublishReq
                                .class);
        var lifecycleMethod =
                SysMessageServiceImpl.class.getMethod(
                        "revokeSourceMessage", String.class, String.class, String.class);

        assertThat(publishMethod.getAnnotation(DistributedLock.class))
                .satisfies(
                        lock -> {
                            assertThat(lock.waitTime()).isEqualTo(10_000);
                            assertThat(lock.key())
                                    .contains(
                                            "#req.sourceType",
                                            "#req.sourceId",
                                            "#req.receiverType");
                        });
        assertThat(lifecycleMethod.getAnnotation(DistributedLock.class))
                .satisfies(
                        lock -> {
                            assertThat(lock.waitTime()).isEqualTo(10_000);
                            assertThat(lock.key())
                                    .contains("#sourceType", "#sourceId", "#receiverType");
                        });
    }

    private SysMessageServiceImpl newService(
            SysMessageMapper mapper, SysMessageChannelHandlerRegistry registry) {
        var service =
                new SysMessageServiceImpl(
                        mock(SysMessageReceiverService.class),
                        mock(SysMessageTemplateService.class),
                        mock(SysMessageConverter.class),
                        mock(WebSocketMessageSender.class),
                        mock(SysFileApi.class),
                        registry,
                        mock(SysMessageScheduledPushScheduler.class),
                        mock(SysUserApi.class),
                        mock(SysRoleApi.class),
                        mock(SysDeptApi.class));
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        return service;
    }
}
