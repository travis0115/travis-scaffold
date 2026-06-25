package com.travis.monolith.system.notice.internal.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.system.notice.api.request.SysNoticeCreateReq;
import com.travis.monolith.system.notice.api.request.SysNoticePageReq;
import com.travis.monolith.system.notice.api.request.SysNoticeUpdateReq;
import com.travis.monolith.system.notice.api.response.SysNoticeResp;
import com.travis.monolith.system.notice.internal.entity.SysNotice;
import com.travis.monolith.system.notice.internal.mapper.SysNoticeMapper;
import com.travis.monolith.system.notice.internal.service.SysNoticeService;
import java.time.LocalDateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@CacheConfig(cacheNames = "system:notice")
public class SysNoticeServiceImpl extends ServiceImplX<SysNoticeMapper, SysNotice>
        implements SysNoticeService {

    @Override
    public PageResp<SysNoticeResp> page(SysNoticePageReq req) {
        var wrapper =
                new LambdaQueryWrapperX<SysNotice>()
                        .likeIfPresent(SysNotice::getTitle, req.getTitle())
                        .eqIfPresent(SysNotice::getStatus, req.getStatus())
                        .orderByDesc(SysNotice::getPinned)
                        .orderByAsc(SysNotice::getSort)
                        .orderByDesc(SysNotice::getPublishTime)
                        .orderByDesc(SysNotice::getCreateTime);
        Page<SysNotice> page = page(req.getPageNum(), req.getPageSize(), wrapper);
        return PageConverter.toResp(page.convert(this::toResp));
    }

    @Override
    @Cacheable(key = "'detail:'+#id")
    public SysNoticeResp get(Long id) {
        return toResp(getByIdOrThrow(id));
    }

    @Override
    @Transactional
    public void create(SysNoticeCreateReq req) {
        var entity = new SysNotice();
        BeanUtils.copyProperties(req, entity);
        prepareForPublish(entity);
        save(entity);
    }

    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void update(Long id, SysNoticeUpdateReq req) {
        var entity = getByIdOrThrow(id);
        BeanUtils.copyProperties(req, entity);
        prepareForPublish(entity);
        updateById(entity);
    }

    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void updateStatus(Long id, Integer status) {
        var entity = getByIdOrThrow(id);
        entity.setStatus(status);
        prepareForPublish(entity);
        updateById(entity);
    }

    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void delete(Long id) {
        removeById(id);
    }

    private void prepareForPublish(SysNotice entity) {
        if (Integer.valueOf(1).equals(entity.getStatus()) && entity.getPublishTime() == null) {
            entity.setPublishTime(LocalDateTime.now());
        }
    }

    private SysNoticeResp toResp(SysNotice entity) {
        var response = new SysNoticeResp();
        BeanUtils.copyProperties(entity, response);
        return response;
    }
}
