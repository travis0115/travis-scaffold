package com.travis.monolith.ops.job.internal.quartz;

import com.travis.infrastructure.framework.quartz.core.QuartzJobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** 用于验证后台任务创建、调度和执行日志链路的测试处理器。 */
@Component
@Slf4j
public class OpsTestJobHandler implements QuartzJobHandler {

    private static final String HANDLER_NAME = "opsTestJob";

    @Override
    public String getName() {
        return HANDLER_NAME;
    }

    @Override
    public String getDescription() {
        return "测试任务";
    }

    @Override
    public void execute(String params) {
        int i = 1 / 0;
        log.info("Ops 测试任务执行成功");
    }
}
