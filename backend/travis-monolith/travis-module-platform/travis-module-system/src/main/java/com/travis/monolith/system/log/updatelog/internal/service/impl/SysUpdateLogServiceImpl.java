package com.travis.monolith.system.log.updatelog.internal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResult;
import com.travis.infrastructure.framework.web.core.exception.BizException;
import com.travis.monolith.system.log.updatelog.api.request.SysUpdateLogReq;
import com.travis.monolith.system.log.updatelog.api.response.SysUpdateLogResp;
import com.travis.monolith.system.log.updatelog.internal.converter.SysUpdateLogConverter;
import com.travis.monolith.system.log.updatelog.internal.entity.SysUpdateLog;
import com.travis.monolith.system.log.updatelog.internal.mapper.SysUpdateLogMapper;
import com.travis.monolith.system.log.updatelog.internal.service.SysUpdateLogService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统更新日志服务实现
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
public class SysUpdateLogServiceImpl extends ServiceImpl<SysUpdateLogMapper, SysUpdateLog>
        implements SysUpdateLogService {

    private final SysUpdateLogConverter converter;

    @Override
    public PageResult<SysUpdateLogResp> page(
            String version, String title, Integer status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SysUpdateLog> wrapper =
                new LambdaQueryWrapper<SysUpdateLog>()
                        .like(version != null, SysUpdateLog::getVersion, version)
                        .like(title != null, SysUpdateLog::getTitle, title)
                        .eq(status != null, SysUpdateLog::getStatus, status)
                        .orderByDesc(SysUpdateLog::getCreateTime);
        Page<SysUpdateLog> page = page(new Page<>(pageNum, pageSize), wrapper);
        List<SysUpdateLogResp> records = converter.toRespList(page.getRecords());
        return new PageResult<>(
                records,
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                page.getPages());
    }

    @Override
    public SysUpdateLogResp getById(Long id) {
        SysUpdateLog updateLog = super.getById(id);
        if (updateLog == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        return converter.toResp(updateLog);
    }

    @Override
    @Transactional
    public void create(SysUpdateLogReq req) {
        SysUpdateLog entity = converter.toEntity(req);
        save(entity);
    }

    @Override
    @Transactional
    public void update(Long id, SysUpdateLogReq req) {
        SysUpdateLog entity = super.getById(id);
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
    public List<SysUpdateLogResp> listPublished(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        LambdaQueryWrapper<SysUpdateLog> wrapper =
                new LambdaQueryWrapper<SysUpdateLog>()
                        .eq(SysUpdateLog::getStatus, 1)
                        .orderByDesc(SysUpdateLog::getPublishTime);
        Page<SysUpdateLog> page = page(new Page<>(1, limit), wrapper);
        return converter.toRespList(page.getRecords());
    }
}
