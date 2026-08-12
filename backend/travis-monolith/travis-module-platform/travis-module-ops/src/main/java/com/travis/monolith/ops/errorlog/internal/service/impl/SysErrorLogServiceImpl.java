package com.travis.monolith.ops.errorlog.internal.service.impl;

import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.ops.errorlog.api.request.SysErrorLogPageReq;
import com.travis.monolith.ops.errorlog.api.response.SysErrorLogResp;
import com.travis.monolith.ops.errorlog.internal.converter.SysErrorLogConverter;
import com.travis.monolith.ops.errorlog.internal.entity.SysErrorLog;
import com.travis.monolith.ops.errorlog.internal.mapper.SysErrorLogMapper;
import com.travis.monolith.ops.errorlog.internal.service.SysErrorLogService;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 系统异常日志服务实现，负责按请求条件分页查询异常日志。 */
@Service
@RequiredArgsConstructor
public class SysErrorLogServiceImpl extends ServiceImplX<SysErrorLogMapper, SysErrorLog>
        implements SysErrorLogService {
    private final SysErrorLogConverter converter;

    @Override
    public PageResp<SysErrorLogResp> page(SysErrorLogPageReq req) {
        var requestMethod =
                Optional.ofNullable(req.getRequestMethod())
                        .map(String::trim)
                        .map(value -> value.toUpperCase(Locale.ROOT))
                        .orElse(null);
        var wrapper =
                new LambdaQueryWrapperX<SysErrorLog>()
                        .likeIfPresent(SysErrorLog::getExceptionClass, req.getExceptionClass())
                        .likeIfPresent(SysErrorLog::getRequestUrl, req.getRequestUrl())
                        .eqIfPresent(SysErrorLog::getRequestMethod, requestMethod)
                        .likeIfPresent(SysErrorLog::getIp, req.getIp())
                        .geIfPresent(SysErrorLog::getCreateTime, req.getStartTime())
                        .leIfPresent(SysErrorLog::getCreateTime, req.getEndTime())
                        .orderByDesc(SysErrorLog::getCreateTime);
        return PageConverter.toResp(
                page(req.getPageNum(), req.getPageSize(), wrapper).convert(converter::toResp));
    }
}
