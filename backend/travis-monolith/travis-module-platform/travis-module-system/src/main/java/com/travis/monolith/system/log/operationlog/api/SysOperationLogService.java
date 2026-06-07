package com.travis.monolith.system.log.operationlog.api;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResult;
import com.travis.monolith.system.log.operationlog.internal.model.entity.SysOperationLog;
import java.time.LocalDateTime;

/**
 * 操作日志服务接口，提供操作日志的分页查询，支持按时间范围筛选
 *
 * @author travis
 */
public interface SysOperationLogService extends IService<SysOperationLog> {

    /**
     * 分页查询操作日志
     *
     * @param username 操作用户名（模糊匹配，可为空）
     * @param module 操作模块（模糊匹配，可为空）
     * @param status 操作状态（可为空）
     * @param startTime 操作开始时间（可为空）
     * @param endTime 操作结束时间（可为空）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResult<SysOperationLog> getOperationLogPage(
            String username,
            String module,
            Integer status,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer pageNum,
            Integer pageSize);
}
