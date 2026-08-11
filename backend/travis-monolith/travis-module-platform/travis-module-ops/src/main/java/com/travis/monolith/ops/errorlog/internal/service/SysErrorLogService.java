package com.travis.monolith.ops.errorlog.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.ops.errorlog.api.request.SysErrorLogPageReq;
import com.travis.monolith.ops.errorlog.api.response.SysErrorLogResp;
import com.travis.monolith.ops.errorlog.internal.entity.SysErrorLog;

/** 系统异常日志查询服务。 */
public interface SysErrorLogService extends IService<SysErrorLog> {
    /** 分页查询系统异常日志。 */
    PageResp<SysErrorLogResp> page(SysErrorLogPageReq req);
}
