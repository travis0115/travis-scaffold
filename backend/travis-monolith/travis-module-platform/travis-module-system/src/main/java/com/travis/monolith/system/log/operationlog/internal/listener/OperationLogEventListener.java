package com.travis.monolith.system.log.operationlog.internal.listener;

import com.travis.monolith.system.log.operationlog.api.event.OperationLogEvent;
import com.travis.monolith.system.log.operationlog.internal.entity.SysOperationLog;
import com.travis.monolith.system.log.operationlog.internal.service.SysOperationLogService;
import com.travis.monolith.system.user.internal.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/** 异步保存操作日志，失败时只记录应用日志，不影响原业务请求。 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OperationLogEventListener {

    private final SysOperationLogService operationLogService;
    private final SysUserMapper userMapper;

    @Async("operationLogTaskExecutor")
    @EventListener
    public void handle(OperationLogEvent event) {
        try {
            var operationLog = new SysOperationLog();
            operationLog.setUserId(event.userId());
            if (event.userId() != null) {
                var user = userMapper.selectById(event.userId());
                if (user != null) {
                    operationLog.setUsername(user.getUsername());
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
        } catch (Exception e) {
            log.error("操作日志写入失败", e);
        }
    }
}
