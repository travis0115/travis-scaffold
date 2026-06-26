package com.travis.monolith.system.notice.internal.service.impl;

import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.notice.api.request.SysNoticeCreateReq;
import com.travis.monolith.system.notice.api.request.SysNoticePageReq;
import com.travis.monolith.system.notice.api.request.SysNoticeUpdateReq;
import com.travis.monolith.system.notice.api.response.SysNoticeResp;
import com.travis.monolith.system.notice.internal.converter.SysNoticeConverter;
import com.travis.monolith.system.notice.internal.entity.SysNotice;
import com.travis.monolith.system.notice.internal.mapper.SysNoticeMapper;
import com.travis.monolith.system.notice.internal.service.SysNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "system:notice")
public class SysNoticeServiceImpl extends ServiceImplX<SysNoticeMapper, SysNotice>
        implements SysNoticeService {

    /** 对象转换器 */
    private final SysNoticeConverter converter;

    @Override
    public PageResp<SysNoticeResp> page(SysNoticePageReq req) {
        var wrapper =
                new LambdaQueryWrapperX<SysNotice>()
                        .likeIfPresent(SysNotice::getTitle, req.getTitle())
                        .eqIfPresent(SysNotice::getStatus, req.getStatus())
                        .orderByDesc(SysNotice::getIsPinned)
                        .orderByAsc(SysNotice::getSort)
                        .orderByDesc(SysNotice::getPublishTime)
                        .orderByDesc(SysNotice::getCreateTime);
        var page = page(req.getPageNum(), req.getPageSize(), wrapper);
        return PageConverter.toResp(page.convert(converter::toResp));
    }

    @Override
    public PageResp<SysNoticeResp> pagePublished(SysNoticePageReq req) {
        var wrapper = new LambdaQueryWrapperX<SysNotice>();
        wrapper.likeIfPresent(SysNotice::getTitle, req.getTitle())
                .eq(SysNotice::getStatus, Status.ENABLED.getValue())
                .le(SysNotice::getPublishTime, LocalDateTime.now())
                .orderByDesc(SysNotice::getIsPinned)
                .orderByAsc(SysNotice::getSort)
                .orderByDesc(SysNotice::getPublishTime)
                .orderByDesc(SysNotice::getCreateTime);
        var page = page(req.getPageNum(), req.getPageSize(), wrapper);
        return PageConverter.toResp(page.convert(converter::toResp));
    }

    @Override
    @Cacheable(key = "'detail:'+#id")
    public SysNoticeResp get(Long id) {
        return converter.toResp(getByIdOrThrow(id));
    }

    @Override
    @Transactional
    public void create(SysNoticeCreateReq req) {
        var entity = converter.toEntity(req);
        save(entity);
    }

    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void update(Long id, SysNoticeUpdateReq req) {
        var entity = getByIdOrThrow(id);
        converter.update(req, entity);
        updateById(entity);
    }

    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void updateStatus(Long id, Integer status) {
        var entity = getByIdOrThrow(id);
        entity.setStatus(status);
        updateById(entity);
    }

    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void delete(Long id) {
        removeById(id);
    }
}
