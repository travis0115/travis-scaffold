package com.travis.monolith.system.notice.internal.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.system.notice.api.request.SysAnnouncementCreateReq;
import com.travis.monolith.system.notice.api.request.SysAnnouncementPageReq;
import com.travis.monolith.system.notice.api.request.SysAnnouncementUpdateReq;
import com.travis.monolith.system.notice.api.response.SysAnnouncementResp;
import com.travis.monolith.system.notice.internal.entity.SysAnnouncement;
import com.travis.monolith.system.notice.internal.mapper.SysAnnouncementMapper;
import com.travis.monolith.system.notice.internal.service.SysAnnouncementService;
import java.time.LocalDateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@CacheConfig(cacheNames = "system:announcement")
public class SysAnnouncementServiceImpl extends ServiceImplX<SysAnnouncementMapper, SysAnnouncement>
        implements SysAnnouncementService {

    @Override
    public PageResp<SysAnnouncementResp> page(SysAnnouncementPageReq req) {
        var wrapper =
                new LambdaQueryWrapperX<SysAnnouncement>()
                        .likeIfPresent(SysAnnouncement::getTitle, req.getTitle())
                        .eqIfPresent(SysAnnouncement::getStatus, req.getStatus())
                        .orderByDesc(SysAnnouncement::getPinned)
                        .orderByAsc(SysAnnouncement::getSort)
                        .orderByDesc(SysAnnouncement::getPublishTime)
                        .orderByDesc(SysAnnouncement::getCreateTime);
        Page<SysAnnouncement> page = page(req.getPageNum(), req.getPageSize(), wrapper);
        return PageConverter.toResp(page.convert(this::toResp));
    }

    @Override
    @Cacheable(key = "'detail:'+#id")
    public SysAnnouncementResp get(Long id) {
        return toResp(getByIdOrThrow(id));
    }

    @Override
    @Transactional
    public void create(SysAnnouncementCreateReq req) {
        var entity = new SysAnnouncement();
        BeanUtils.copyProperties(req, entity);
        prepareForPublish(entity);
        save(entity);
    }

    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void update(Long id, SysAnnouncementUpdateReq req) {
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

    private void prepareForPublish(SysAnnouncement entity) {
        if (Integer.valueOf(1).equals(entity.getStatus()) && entity.getPublishTime() == null) {
            entity.setPublishTime(LocalDateTime.now());
        }
    }

    private SysAnnouncementResp toResp(SysAnnouncement entity) {
        var response = new SysAnnouncementResp();
        BeanUtils.copyProperties(entity, response);
        return response;
    }
}
