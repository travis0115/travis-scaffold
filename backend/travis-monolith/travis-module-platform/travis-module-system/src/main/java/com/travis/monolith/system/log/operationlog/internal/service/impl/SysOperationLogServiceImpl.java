package com.travis.monolith.system.log.operationlog.internal.service.impl;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.system.log.operationlog.api.request.SysOperationLogPageReq;
import com.travis.monolith.system.log.operationlog.api.response.SysOperationLogResp;
import com.travis.monolith.system.log.operationlog.internal.converter.SysOperationLogConverter;
import com.travis.monolith.system.log.operationlog.internal.entity.SysOperationLog;
import com.travis.monolith.system.log.operationlog.internal.mapper.SysOperationLogMapper;
import com.travis.monolith.system.log.operationlog.internal.service.SysOperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 操作日志服务实现，支持按用户名、模块、状态及时间范围分页查询
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
public class SysOperationLogServiceImpl extends ServiceImplX<SysOperationLogMapper, SysOperationLog>
        implements SysOperationLogService {

    private final SysOperationLogConverter converter;

    private static final Map<String, SFunction<SysOperationLog, ?>> SORT_COLUMNS =
            Map.ofEntries(
                    Map.entry("duration", SysOperationLog::getDuration),
                    Map.entry("createTime", SysOperationLog::getCreateTime));

    /** 分页查询操作日志，支持多条件筛选，按创建时间倒序排列 */
    @Override
    public PageResp<SysOperationLogResp> page(SysOperationLogPageReq req) {
        var wrapper =
                new LambdaQueryWrapperX<SysOperationLog>()
                        .likeIfPresent(SysOperationLog::getUsername, req.getUsername())
                        .likeIfPresent(SysOperationLog::getModule, req.getModule())
                        .likeIfPresent(SysOperationLog::getBusinessType, req.getBusinessType())
                        .likeIfPresent(SysOperationLog::getRequestUrl, req.getRequestUrl())
                        .likeIfPresent(SysOperationLog::getRequestId, req.getRequestId())
                        .likeIfPresent(SysOperationLog::getIp, req.getIp())
                        .eqIfPresent(SysOperationLog::getStatus, req.getStatus())
                        .geIfPresent(SysOperationLog::getCreateTime, req.getStartTime())
                        .leIfPresent(SysOperationLog::getCreateTime, req.getEndTime())
                        .orderByAllowed(
                                req.getOrderBy(),
                                req.getAsc(),
                                SORT_COLUMNS,
                                false,
                                SysOperationLog::getCreateTime);
        var page = page(req.getPageNum(), req.getPageSize(), wrapper);
        return PageConverter.toResp(page.convert(converter::toResp));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void create(SysOperationLog operationLog) {
        save(operationLog);
    }
}
