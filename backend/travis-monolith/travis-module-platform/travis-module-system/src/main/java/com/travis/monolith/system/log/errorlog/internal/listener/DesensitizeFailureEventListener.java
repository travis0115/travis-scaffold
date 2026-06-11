package com.travis.monolith.system.log.errorlog.internal.listener;

import com.travis.infrastructure.framework.web.core.event.DesensitizeFailureEvent;
import com.travis.monolith.system.log.errorlog.internal.entity.SysErrorLog;
import com.travis.monolith.system.log.errorlog.internal.service.SysErrorLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** 监听脱敏失败事件，写入错误日志 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DesensitizeFailureEventListener {

    private final SysErrorLogService errorLogService;

    @EventListener
    public void onDesensitizeFailure(DesensitizeFailureEvent event) {
        try {
            var errorLog = new SysErrorLog();
            errorLog.setRequestUrl(event.getRequestUrl());
            errorLog.setRequestMethod(event.getHttpMethod());
            errorLog.setExceptionClass(event.getExceptionClass());
            errorLog.setMessage("脱敏失败: " + event.getMessage());
            errorLog.setStackTrace(event.getStackTrace());
            errorLogService.saveError(errorLog);
        } catch (Exception e) {
            log.error("脱敏失败事件处理异常", e);
        }
    }
}
