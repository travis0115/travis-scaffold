package com.travis.monolith.system.log.versionlog.internal.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.web.core.exception.BizException;
import com.travis.monolith.system.log.versionlog.api.request.SysVersionLogReq;
import com.travis.monolith.system.log.versionlog.api.response.SysVersionLogResp;
import com.travis.monolith.system.log.versionlog.internal.converter.SysVersionLogConverter;
import com.travis.monolith.system.log.versionlog.internal.entity.SysVersionLog;
import com.travis.monolith.system.log.versionlog.internal.mapper.SysVersionLogMapper;
import com.travis.monolith.system.log.versionlog.internal.service.SysVersionLogService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统版本日志服务实现
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
public class SysVersionLogServiceImpl extends ServiceImpl<SysVersionLogMapper, SysVersionLog>
        implements SysVersionLogService {

    private final SysVersionLogConverter converter;

    @Override
    public PageResp<SysVersionLogResp> page(
            String version, String title, Integer status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapperX<SysVersionLog> wrapper =
                new LambdaQueryWrapperX<SysVersionLog>()
                        .likeIfPresent(SysVersionLog::getVersion, version)
                        .likeIfPresent(SysVersionLog::getTitle, title)
                        .eqIfPresent(SysVersionLog::getStatus, status)
                        .orderByDesc(SysVersionLog::getCreateTime);
        Page<SysVersionLog> page = page(new Page<>(pageNum, pageSize), wrapper);
        return PageConverter.toResp(page.convert(converter::toResp));
    }

    @Override
    public SysVersionLogResp getById(Long id) {
        SysVersionLog versionLog = super.getById(id);
        if (versionLog == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        return converter.toResp(versionLog);
    }

    @Override
    @Transactional
    public void create(SysVersionLogReq req) {
        SysVersionLog entity = converter.toEntity(req);
        save(entity);
    }

    @Override
    @Transactional
    public void update(Long id, SysVersionLogReq req) {
        SysVersionLog entity = super.getById(id);
        if (entity == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        converter.update(req, entity);
        updateById(entity);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        removeById(id);
    }

    @Override
    public List<SysVersionLogResp> listPublished(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        LambdaQueryWrapperX<SysVersionLog> wrapper =
                new LambdaQueryWrapperX<SysVersionLog>()
                        .eq(SysVersionLog::getStatus, 1)
                        .orderByDesc(SysVersionLog::getPublishTime);
        Page<SysVersionLog> page = page(new Page<>(1, limit), wrapper);
        return converter.toRespList(page.getRecords());
    }
}
