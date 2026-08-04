package com.travis.monolith.system.message.internal.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.travis.infrastructure.framework.websocket.core.sender.WebSocketMessageSender;
import com.travis.monolith.system.file.api.SysFileApi;
import com.travis.monolith.system.message.api.SysMessageSourceContentProvider;
import com.travis.monolith.system.message.api.enums.SysMessageReadStatus;
import com.travis.monolith.system.message.internal.converter.SysMessageReceiverConverter;
import com.travis.monolith.system.message.internal.entity.SysMessageReceiver;
import com.travis.monolith.system.message.internal.mapper.SysMessageMapper;
import com.travis.monolith.system.message.internal.mapper.SysMessageReceiverMapper;
import com.travis.monolith.system.role.api.SysRoleApi;
import com.travis.monolith.system.user.api.SysUserApi;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;

class SysMessageReceiverServiceImplTest {

    @Test
    @SuppressWarnings("unchecked")
    void shouldProcessAllUnreadMessagesWithBoundedCursorBatches() {
        SysMessageReceiverMapper mapper = mock(SysMessageReceiverMapper.class);
        WebSocketMessageSender sender = mock(WebSocketMessageSender.class);
        ObjectProvider<SysMessageSourceContentProvider> providers = mock(ObjectProvider.class);
        var service =
                new SysMessageReceiverServiceImpl(
                        mock(SysMessageReceiverConverter.class),
                        mock(SysUserApi.class),
                        mock(SysRoleApi.class),
                        sender,
                        mock(SysFileApi.class),
                        providers,
                        mock(SysMessageMapper.class));
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        when(mapper.selectInboxMessageIds(
                        9L,
                        "app",
                        List.of(),
                        null,
                        SysMessageReadStatus.UNREAD.getValue(),
                        null,
                        500))
                .thenReturn(List.of(1L, 2L));
        when(mapper.selectInboxMessageIds(
                        9L,
                        "app",
                        List.of(),
                        null,
                        SysMessageReadStatus.UNREAD.getValue(),
                        2L,
                        500))
                .thenReturn(List.of(3L));
        when(mapper.selectInboxMessageIds(
                        9L,
                        "app",
                        List.of(),
                        null,
                        SysMessageReadStatus.UNREAD.getValue(),
                        3L,
                        500))
                .thenReturn(List.of());

        service.markAllRead("app", 9L);

        ArgumentCaptor<List<SysMessageReceiver>> states =
                (ArgumentCaptor<List<SysMessageReceiver>>)
                        (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);
        verify(mapper, times(2)).upsertStates(states.capture());
        assertThat(states.getAllValues().get(0)).hasSize(2);
        assertThat(states.getAllValues().get(1)).hasSize(1);
        assertThat(states.getAllValues())
                .allSatisfy(
                        batch ->
                                assertThat(batch)
                                        .allMatch(
                                                state ->
                                                        SysMessageReadStatus.READ
                                                                .getValue()
                                                                .equals(state.getReadStatus())));
        verify(sender).sendToPrincipal(any(), any());
    }
}
