package com.travis.monolith.ops.job.internal.quartz;

import com.travis.infrastructure.framework.quartz.core.QuartzJobHandler;
import com.travis.monolith.ops.job.internal.service.OpsJobLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 定期清理过期任务执行日志的 Quartz 内部任务。 */
@RequiredArgsConstructor
@Component
public class OpsJobLogCleanupJob implements QuartzJobHandler {

    private static final String HANDLER_NAME = "opsJobLogCleanup";

    private final OpsJobLogService logService;

    @Override
    public String getName() {
        return HANDLER_NAME;
    }

    @Override
    public String getDescription() {
        return "清理过期任务执行日志";
    }

    @Override
    public boolean isBuiltin() {
        return true;
    }

    @Override
    public void execute(String params) {
        logService.cleanExpired();
    }
}
