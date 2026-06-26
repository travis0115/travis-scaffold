package com.travis.monolith.ops.errorlog.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.ops.errorlog.api.request.SysErrorLogPageReq;
import com.travis.monolith.ops.errorlog.internal.entity.SysErrorLog;

public interface SysErrorLogService extends IService<SysErrorLog> {
    PageResp<SysErrorLog> page(SysErrorLogPageReq req);
}
