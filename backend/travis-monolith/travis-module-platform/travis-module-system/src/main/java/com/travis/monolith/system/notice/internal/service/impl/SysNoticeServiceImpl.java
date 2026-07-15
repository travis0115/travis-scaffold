package com.travis.monolith.system.notice.internal.service.impl;

import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.system.common.api.enums.PublishStatus;
import com.travis.monolith.system.file.api.SysFileApi;
import com.travis.monolith.system.message.api.SysMessageApi;
import com.travis.monolith.system.message.api.enums.SysMessageReceiverScope;
import com.travis.monolith.system.message.api.enums.SysMessageSourceType;
import com.travis.monolith.system.message.api.enums.SysMessageType;
import com.travis.monolith.system.message.api.request.SysSourceMessagePublishReq;
import com.travis.monolith.system.notice.api.request.SysNoticeCreateReq;
import com.travis.monolith.system.notice.api.request.SysNoticePageReq;
import com.travis.monolith.system.notice.api.request.SysNoticeUpdateReq;
import com.travis.monolith.system.notice.api.response.SysNoticeResp;
import com.travis.monolith.system.notice.internal.converter.SysNoticeConverter;
import com.travis.monolith.system.notice.internal.entity.SysNotice;
import com.travis.monolith.system.notice.internal.mapper.SysNoticeMapper;
import com.travis.monolith.system.notice.internal.service.SysNoticeService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 系统公告服务实现，负责公告查询维护、发布状态及置顶状态管理。 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "system:notice")
public class SysNoticeServiceImpl extends ServiceImplX<SysNoticeMapper, SysNotice>
        implements SysNoticeService {

    /** 对象转换器 */
    private final SysNoticeConverter converter;

    private final SysFileApi fileApi;

    private final SysMessageApi messageApi;

    /** 分页查询系统公告。 */
    @Override
    public PageResp<SysNoticeResp> page(SysNoticePageReq req) {
        var wrapper =
                new LambdaQueryWrapperX<SysNotice>()
                        .likeIfPresent(SysNotice::getTitle, req.getTitle())
                        .eqIfPresent(SysNotice::getStatus, req.getStatus())
                        .geIfPresent(
                                SysNotice::getPublishTime,
                                req.getPublishStartDate() == null
                                        ? null
                                        : req.getPublishStartDate().atStartOfDay())
                        .ltIfPresent(
                                SysNotice::getPublishTime,
                                req.getPublishEndDate() == null
                                        ? null
                                        : req.getPublishEndDate().plusDays(1).atStartOfDay())
                        .orderByDesc(SysNotice::getIsPinned)
                        .orderByAsc(SysNotice::getSort)
                        .orderByDesc(SysNotice::getPublishTime)
                        .orderByDesc(SysNotice::getCreateTime);
        var page = page(req.getPageNum(), req.getPageSize(), wrapper);
        return PageConverter.toResp(page.convert(this::toResp));
    }

    /** 分页查询已发布的系统公告。 */
    @Override
    public PageResp<SysNoticeResp> pagePublished(SysNoticePageReq req) {
        var wrapper = new LambdaQueryWrapperX<SysNotice>();
        wrapper.likeIfPresent(SysNotice::getTitle, req.getTitle())
                .geIfPresent(
                        SysNotice::getPublishTime,
                        req.getPublishStartDate() == null
                                ? null
                                : req.getPublishStartDate().atStartOfDay())
                .ltIfPresent(
                        SysNotice::getPublishTime,
                        req.getPublishEndDate() == null
                                ? null
                                : req.getPublishEndDate().plusDays(1).atStartOfDay())
                .eq(SysNotice::getStatus, PublishStatus.PUBLISHED.getValue())
                .le(SysNotice::getPublishTime, LocalDateTime.now())
                .orderByDesc(SysNotice::getIsPinned)
                .orderByAsc(SysNotice::getSort)
                .orderByDesc(SysNotice::getPublishTime)
                .orderByDesc(SysNotice::getCreateTime);
        var page = page(req.getPageNum(), req.getPageSize(), wrapper);
        return PageConverter.toResp(page.convert(this::toResp));
    }

    /** 查询指定系统公告，不存在时抛出业务异常。 */
    @Override
    @Cacheable(key = "'detail:'+#id")
    public SysNoticeResp getOrThrow(Long id) {
        return toResp(getByIdOrThrow(id));
    }

    /** 创建系统公告。 */
    @Override
    @Transactional
    public void create(SysNoticeCreateReq req) {
        if (PublishStatus.REVOKED.getValue().equals(req.getStatus())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "新建公告不能设置为已撤回");
        }
        var entity = converter.toEntity(req);
        entity.setContent(fileApi.stripManagedImageSources(entity.getContent()));
        entity.setPublishTime(
                PublishStatus.PUBLISHED.getValue().equals(entity.getStatus())
                        ? LocalDateTime.now()
                        : null);
        save(entity);
        if (PublishStatus.PUBLISHED.getValue().equals(entity.getStatus())) {
            messageApi.publishSourceMessage(toMessageRequest(entity));
        }
    }

    /** 更新指定系统公告。 */
    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void update(Long id, SysNoticeUpdateReq req) {
        var entity = getByIdOrThrow(id);
        if (PublishStatus.PUBLISHED.getValue().equals(entity.getStatus())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "已发布公告请先撤回");
        }
        var normalizedContent = fileApi.stripManagedImageSources(req.getContent());
        converter.update(req, entity);
        entity.setContent(normalizedContent);
        updateById(entity);
    }

    /** 更新指定系统公告的状态。 */
    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void updateStatus(Long id, Integer status) {
        var entity = getByIdOrThrow(id);
        if (status.equals(entity.getStatus())) {
            return;
        }
        if (PublishStatus.PUBLISHED.getValue().equals(status)) {
            entity.setStatus(status);
            entity.setPublishTime(LocalDateTime.now());
            updateById(entity);
            messageApi.publishSourceMessage(toMessageRequest(entity));
        } else if (PublishStatus.REVOKED.getValue().equals(status)
                && PublishStatus.PUBLISHED.getValue().equals(entity.getStatus())) {
            entity.setStatus(status);
            updateById(entity);
            messageApi.revokeSourceMessage(
                    SysMessageSourceType.NOTICE.getValue(), id.toString(), LoginType.ADMIN);
        } else {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "公告发布状态流转不合法");
        }
    }

    /** 更新系统公告的置顶状态。 */
    @Override
    @CacheEvict(key = "'detail:'+#id")
    public void updatePinned(Long id, Integer isPinned) {
        var entity = getByIdOrThrow(id);
        entity.setIsPinned(isPinned);
        updateById(entity);
    }

    /** 删除指定系统公告。 */
    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void delete(Long id) {
        var entity = getByIdOrThrow(id);
        if (PublishStatus.PUBLISHED.getValue().equals(entity.getStatus())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "已发布公告请先撤回");
        }
        messageApi.deleteSourceMessage(
                SysMessageSourceType.NOTICE.getValue(), id.toString(), LoginType.ADMIN);
        removeById(id);
    }

    /** 将系统公告转换为消息发布请求。 */
    private SysSourceMessagePublishReq toMessageRequest(SysNotice notice) {
        var req = new SysSourceMessagePublishReq();
        req.setMessageType(SysMessageType.NOTICE.getValue());
        req.setSourceType(SysMessageSourceType.NOTICE.getValue());
        req.setSourceId(notice.getId().toString());
        req.setTitle(notice.getTitle());
        req.setReceiverType(LoginType.ADMIN);
        req.setReceiverScope(SysMessageReceiverScope.ALL.getValue());
        req.setPublishTime(notice.getPublishTime());
        return req;
    }

    /** 将系统公告实体转换并补充为响应对象。 */
    private SysNoticeResp toResp(SysNotice entity) {
        var resp = converter.toResp(entity);
        resp.setContent(fileApi.resolveManagedImageSources(resp.getContent()));
        return resp;
    }
}
