package com.travis.monolith.system.log.operationlog.internal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.common.web.model.PageResult;
import com.travis.monolith.system.log.operationlog.internal.entity.SysOperationLog;
import com.travis.monolith.system.log.operationlog.internal.mapper.SysOperationLogMapper;
import com.travis.monolith.system.log.operationlog.internal.service.SysOperationLogService;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务实现，支持按用户名、模块、状态及时间范围分页查询
 *
 * @author travis
 */
@Service
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog>
        implements SysOperationLogService {

    /** 分页查询操作日志，支持多条件筛选，按创建时间倒序排列 */
    @Override
    public PageResult<SysOperationLog> page(
            String username,
            String module,
            Integer status,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer pageNum,
            Integer pageSize) {
        LambdaQueryWrapper<SysOperationLog> wrapper =
                new LambdaQueryWrapper<SysOperationLog>()
                        .like(username != null, SysOperationLog::getUsername, username)
                        .like(module != null, SysOperationLog::getModule, module)
                        .eq(status != null, SysOperationLog::getStatus, status)
                        .ge(startTime != null, SysOperationLog::getCreateTime, startTime)
                        .le(endTime != null, SysOperationLog::getCreateTime, endTime)
                        .orderByDesc(SysOperationLog::getCreateTime);
        Page<SysOperationLog> page = page(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(
                page.getRecords(),
                page.getTotal(),
                (int) page.getCurrent(),
                (int) page.getSize(),
                (int) page.getPages());
    }
}
