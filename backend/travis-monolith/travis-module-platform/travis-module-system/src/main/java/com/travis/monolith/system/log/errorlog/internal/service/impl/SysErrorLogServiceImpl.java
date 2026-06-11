package com.travis.monolith.system.log.errorlog.internal.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.monolith.system.log.errorlog.api.request.SysErrorLogPageReq;
import com.travis.monolith.system.log.errorlog.internal.entity.SysErrorLog;
import com.travis.monolith.system.log.errorlog.internal.mapper.SysErrorLogMapper;
import com.travis.monolith.system.log.errorlog.internal.service.SysErrorLogService;
import org.springframework.stereotype.Service;

@Service
public class SysErrorLogServiceImpl extends ServiceImpl<SysErrorLogMapper, SysErrorLog>
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
        return PageConverter.toResp(page(new Page<>(req.getPageNum(), req.getPageSize()), wrapper));
    }
}
