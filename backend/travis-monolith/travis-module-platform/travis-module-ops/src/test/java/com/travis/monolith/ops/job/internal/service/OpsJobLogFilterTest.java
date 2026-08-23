package com.travis.monolith.ops.job.internal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.monolith.ops.job.api.request.OpsJobLogPageReq;
import com.travis.monolith.ops.job.internal.config.OpsJobProperties;
import com.travis.monolith.ops.job.internal.converter.OpsJobLogConverter;
import com.travis.monolith.ops.job.internal.entity.OpsJobLog;
import com.travis.monolith.ops.job.internal.mapper.OpsJobMapper;
import com.travis.monolith.ops.job.internal.service.impl.OpsJobLogServiceImpl;
import java.time.LocalDateTime;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class OpsJobLogFilterTest {

    @BeforeAll
    static void initializeTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""), OpsJobLog.class);
    }

    @Test
    void shouldFilterByHandlerAndExecutionStartTime() {
        var service =
                new OpsJobLogServiceImpl(
                        mock(OpsJobLogConverter.class),
                        new OpsJobProperties(),
                        mock(OpsJobMapper.class));
        var start = LocalDateTime.of(2026, 8, 19, 10, 0);
        var end = LocalDateTime.of(2026, 8, 19, 11, 0);
        var request = new OpsJobLogPageReq();
        request.setHandlerName("testHandler");
        request.setStartTime(start);
        request.setEndTime(end);

        LambdaQueryWrapperX<OpsJobLog> wrapper =
                ReflectionTestUtils.invokeMethod(service, "buildWrapper", request);

        assertThat(wrapper).isNotNull();
        assertThat(wrapper.getSqlSegment())
                .contains("handler_name", "start_time")
                .doesNotContain("create_time");
        assertThat(wrapper.getParamNameValuePairs())
                .containsValue("testHandler")
                .containsValue(start)
                .containsValue(end);
    }
}
