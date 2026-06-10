package com.travis.monolith.system.log.operationlog.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.log.operationlog.api.request.SysOperationLogPageReq;
import com.travis.monolith.system.log.operationlog.internal.entity.SysOperationLog;

/**
 * 操作日志服务接口，提供操作日志的分页查询，支持按时间范围筛选
 *
 * @author travis
 */
public interface SysOperationLogService extends IService<SysOperationLog> {

    /**
     * 分页查询操作日志
     *
     * @param req 分页查询参数
     * @return 分页结果
     */
    PageResp<SysOperationLog> page(SysOperationLogPageReq req);
}
