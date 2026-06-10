package com.travis.monolith.system.log.operationlog.internal.service.impl;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.monolith.system.log.operationlog.api.request.SysOperationLogPageReq;
import com.travis.monolith.system.log.operationlog.internal.entity.SysOperationLog;
import com.travis.monolith.system.log.operationlog.internal.mapper.SysOperationLogMapper;
import com.travis.monolith.system.log.operationlog.internal.service.SysOperationLogService;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务实现，支持按用户名、模块、状态及时间范围分页查询
 *
 * @author travis
 */
@Service
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog>
        implements SysOperationLogService {

    private static final Map<String, SFunction<SysOperationLog, ?>> SORT_COLUMNS =
            Map.ofEntries(
                    Map.entry("id", SysOperationLog::getId),
                    Map.entry("userId", SysOperationLog::getUserId),
                    Map.entry("username", SysOperationLog::getUsername),
                    Map.entry("module", SysOperationLog::getModule),
                    Map.entry("method", SysOperationLog::getMethod),
                    Map.entry("requestMethod", SysOperationLog::getRequestMethod),
                    Map.entry("ip", SysOperationLog::getIp),
                    Map.entry("duration", SysOperationLog::getDuration),
                    Map.entry("status", SysOperationLog::getStatus),
                    Map.entry("createTime", SysOperationLog::getCreateTime));

    /** 分页查询操作日志，支持多条件筛选，按创建时间倒序排列 */
    @Override
    public PageResp<SysOperationLog> page(SysOperationLogPageReq req) {
        LambdaQueryWrapperX<SysOperationLog> wrapper =
                new LambdaQueryWrapperX<SysOperationLog>()
                        .likeIfPresent(SysOperationLog::getUsername, req.getUsername())
                        .likeIfPresent(SysOperationLog::getModule, req.getModule())
                        .eqIfPresent(SysOperationLog::getStatus, req.getStatus())
                        .geIfPresent(SysOperationLog::getCreateTime, req.getStartTime())
                        .leIfPresent(SysOperationLog::getCreateTime, req.getEndTime())
                        .orderByAllowed(
                                req.getOrderBy(),
                                req.getAsc(),
                                SORT_COLUMNS,
                                false,
                                SysOperationLog::getCreateTime);
        Page<SysOperationLog> page = page(new Page<>(req.getPageNum(), req.getPageSize()), wrapper);
        return PageConverter.toResp(page);
    }
}
