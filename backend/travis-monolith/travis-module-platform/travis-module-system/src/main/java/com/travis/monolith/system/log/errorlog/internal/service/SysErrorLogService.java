package com.travis.monolith.system.log.errorlog.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.log.errorlog.api.request.SysErrorLogPageReq;
import com.travis.monolith.system.log.errorlog.internal.entity.SysErrorLog;

public interface SysErrorLogService extends IService<SysErrorLog> {
    PageResp<SysErrorLog> page(SysErrorLogPageReq req);

    void saveError(SysErrorLog errorLog);
}
