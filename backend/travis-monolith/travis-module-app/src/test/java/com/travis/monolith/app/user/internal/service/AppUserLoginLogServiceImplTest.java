package com.travis.monolith.app.user.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.travis.monolith.app.user.internal.mapper.AppUserLoginLogMapper;
import com.travis.monolith.app.user.internal.service.impl.AppUserLoginLogServiceImpl;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AppUserLoginLogServiceImplTest {

    @Test
    void shouldReturnTodaySuccessfulUserCount() {
        var mapper = mock(AppUserLoginLogMapper.class);
        when(mapper.selectSuccessfulUserCount(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(5L);
        var service = new AppUserLoginLogServiceImpl();
        ReflectionTestUtils.setField(service, "baseMapper", mapper);

        var result = service.dashboard();

        assertThat(result.todayLoginUsers()).isEqualTo(5);
    }
}
