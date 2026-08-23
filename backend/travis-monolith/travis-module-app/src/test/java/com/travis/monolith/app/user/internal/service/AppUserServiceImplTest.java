package com.travis.monolith.app.user.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.framework.websocket.core.session.WebSocketSessionManager;
import com.travis.monolith.app.user.internal.mapper.AppUserMapper;
import com.travis.monolith.app.user.internal.model.AppUserCountSummary;
import com.travis.monolith.app.user.internal.service.impl.AppUserServiceImpl;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;

class AppUserServiceImplTest {

    @Test
    void shouldReturnAppUserCountsAndOnlinePrincipals() {
        var mapper = mock(AppUserMapper.class);
        when(mapper.selectCountSummary(any(LocalDateTime.class)))
                .thenReturn(new AppUserCountSummary(12, 2));
        var sessionManager = mock(WebSocketSessionManager.class);
        when(sessionManager.countConnectedPrincipals(LoginType.APP)).thenReturn(4L);
        @SuppressWarnings("unchecked")
        ObjectProvider<WebSocketSessionManager> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(sessionManager);
        var service = new AppUserServiceImpl(provider);
        ReflectionTestUtils.setField(service, "baseMapper", mapper);

        var result = service.dashboard();

        assertThat(result.totalUsers()).isEqualTo(12);
        assertThat(result.newUsersToday()).isEqualTo(2);
        assertThat(result.onlineUsers()).isEqualTo(4);
    }
}
