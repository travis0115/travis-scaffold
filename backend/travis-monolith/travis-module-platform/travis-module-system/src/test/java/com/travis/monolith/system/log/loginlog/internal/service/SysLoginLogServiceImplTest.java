package com.travis.monolith.system.log.loginlog.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.travis.monolith.system.log.loginlog.internal.converter.SysLoginLogConverter;
import com.travis.monolith.system.log.loginlog.internal.mapper.SysLoginLogMapper;
import com.travis.monolith.system.log.loginlog.internal.service.impl.SysLoginLogServiceImpl;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SysLoginLogServiceImplTest {

    @Test
    void shouldReturnTodaySuccessfulUserCount() {
        var mapper = mock(SysLoginLogMapper.class);
        when(mapper.selectSuccessfulUserCount(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(3L);
        var service = new SysLoginLogServiceImpl(mock(SysLoginLogConverter.class));
        ReflectionTestUtils.setField(service, "baseMapper", mapper);

        var result = service.dashboard();

        assertThat(result.todayLoginUsers()).isEqualTo(3);
    }
}
