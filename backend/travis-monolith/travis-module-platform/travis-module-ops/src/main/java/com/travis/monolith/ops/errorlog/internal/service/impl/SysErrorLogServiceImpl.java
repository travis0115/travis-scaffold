package com.travis.monolith.ops.errorlog.internal.service.impl;

import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.ops.errorlog.api.request.SysErrorLogPageReq;
import com.travis.monolith.ops.errorlog.internal.entity.SysErrorLog;
import com.travis.monolith.ops.errorlog.internal.mapper.SysErrorLogMapper;
import com.travis.monolith.ops.errorlog.internal.service.SysErrorLogService;
import org.springframework.stereotype.Service;

/** 系统异常日志服务实现，负责按请求条件分页查询异常日志。 */
@Service
public class SysErrorLogServiceImpl extends ServiceImplX<SysErrorLogMapper, SysErrorLog>
        implements SysErrorLogService {
    @Override
    public PageResp<SysErrorLog> page(SysErrorLogPageReq req) {
        var wrapper =
                new LambdaQueryWrapperX<SysErrorLog>()
                        .likeIfPresent(SysErrorLog::getExceptionClass, req.getExceptionClass())
                        .likeIfPresent(SysErrorLog::getRequestUrl, req.getRequestUrl())
                        .geIfPresent(SysErrorLog::getCreateTime, req.getStartTime())
                        .leIfPresent(SysErrorLog::getCreateTime, req.getEndTime())
                        .orderByDesc(SysErrorLog::getCreateTime);
        return PageConverter.toResp(page(req.getPageNum(), req.getPageSize(), wrapper));
    }
}
