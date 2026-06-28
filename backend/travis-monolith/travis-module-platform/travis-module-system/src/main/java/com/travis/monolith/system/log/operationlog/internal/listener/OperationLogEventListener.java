package com.travis.monolith.system.log.operationlog.internal.listener;

import com.travis.monolith.system.log.operationlog.api.event.OperationLogEvent;
import com.travis.monolith.system.log.operationlog.internal.entity.SysOperationLog;
import com.travis.monolith.system.log.operationlog.internal.service.SysOperationLogService;
import com.travis.monolith.system.user.api.SysUserApi;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/** 异步保存操作日志。 */
@Component
@RequiredArgsConstructor
public class OperationLogEventListener {

    private final SysOperationLogService operationLogService;
    private final SysUserApi userApi;

    @ApplicationModuleListener
    public void handle(OperationLogEvent event) {
        var operationLog = new SysOperationLog();
        operationLog.setUserId(event.userId());
        if (event.userId() != null) {
            String username = userApi.getUsernameById(event.userId());
            if (username != null) {
                operationLog.setUsername(username);
            }
        }
        operationLog.setDescription(event.description());
        operationLog.setModule(event.module());
        operationLog.setMethod(event.method());
        operationLog.setRequestUrl(event.requestUrl());
        operationLog.setRequestMethod(event.requestMethod());
        operationLog.setRequestParams(event.requestParams());
        operationLog.setResponseResult(event.responseResult());
        operationLog.setIp(event.ip());
        operationLog.setDuration(event.duration());
        operationLog.setStatus(event.status());
        operationLog.setErrorMsg(event.errorMsg());
        operationLogService.saveOperation(operationLog);
    }
}
