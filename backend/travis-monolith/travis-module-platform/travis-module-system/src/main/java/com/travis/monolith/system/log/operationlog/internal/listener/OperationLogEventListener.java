package com.travis.monolith.system.log.operationlog.internal.listener;

import com.travis.infrastructure.framework.web.core.util.Ip2RegionUtil;
import com.travis.monolith.system.log.operationlog.api.event.OperationLogEvent;
import com.travis.monolith.system.log.operationlog.internal.converter.SysOperationLogConverter;
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
    private final SysOperationLogConverter converter;

    @ApplicationModuleListener
    public void handle(OperationLogEvent event) {
        String username = null;
        if (event.userId() != null) {
            username = userApi.getUsernameById(event.userId());
        }
        var operationLog = converter.toEntity(event, username);
        operationLog.setLocation(Ip2RegionUtil.getRegionByIP(event.ip()));
        operationLogService.create(operationLog);
    }
}
