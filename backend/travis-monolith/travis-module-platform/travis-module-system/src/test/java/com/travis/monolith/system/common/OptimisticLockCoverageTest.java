package com.travis.monolith.system.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.annotation.Version;
import com.travis.monolith.system.common.api.BuiltinResourceGuard;
import com.travis.monolith.system.config.api.request.SysConfigUpdateReq;
import com.travis.monolith.system.config.internal.converter.SysConfigConverter;
import com.travis.monolith.system.config.internal.entity.SysConfig;
import com.travis.monolith.system.config.internal.mapper.SysConfigMapper;
import com.travis.monolith.system.config.internal.service.impl.SysConfigServiceImpl;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigUpdateReq;
import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import com.travis.monolith.system.message.api.request.SysMessageTemplateUpdateReq;
import com.travis.monolith.system.message.internal.entity.SysMessageTemplate;
import com.travis.monolith.system.notice.api.request.SysNoticeUpdateReq;
import com.travis.monolith.system.notice.internal.entity.SysNotice;
import com.travis.monolith.system.version.api.request.SysVersionUpdateReq;
import com.travis.monolith.system.version.internal.entity.SysVersion;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class OptimisticLockCoverageTest {

    @Test
    void shouldDeclareVersionFieldAndRequireItOnUpdateRequests() throws Exception {
        for (Class<?> entityType :
                List.of(
                        SysNotice.class,
                        SysVersion.class,
                        SysMessageTemplate.class,
                        SysFileStorageConfig.class,
                        SysConfig.class)) {
            assertThat(entityType.getDeclaredField("lockVersion").getAnnotation(Version.class))
                    .isNotNull();
        }
        for (Class<?> requestType :
                List.of(
                        SysNoticeUpdateReq.class,
                        SysVersionUpdateReq.class,
                        SysMessageTemplateUpdateReq.class,
                        SysFileStorageConfigUpdateReq.class,
                        SysConfigUpdateReq.class)) {
            assertThat(requestType.getDeclaredField("lockVersion").getAnnotation(NotNull.class))
                    .isNotNull();
        }
    }

    @Test
    void shouldRejectStaleSystemConfigUpdate() {
        var mapper = mock(SysConfigMapper.class);
        var converter = mock(SysConfigConverter.class);
        var service = new SysConfigServiceImpl(converter, mock(BuiltinResourceGuard.class));
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        var entity = new SysConfig();
        entity.setId(1L);
        entity.setIsBuiltin(0);
        entity.setLockVersion(2);
        when(mapper.selectById(1L)).thenReturn(entity);
        when(mapper.updateById(entity)).thenReturn(0);
        var request = new SysConfigUpdateReq();
        request.setLockVersion(1);

        assertThatThrownBy(() -> service.update(1L, request)).hasMessageContaining("已被其他请求修改");
    }
}
