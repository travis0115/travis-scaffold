package com.travis.monolith.system.notice.internal.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.monolith.system.notice.api.request.SysNoticeCreateReq;
import com.travis.monolith.system.notice.api.request.SysNoticePageReq;
import com.travis.monolith.system.notice.api.request.SysNoticeUpdateReq;
import com.travis.monolith.system.notice.api.response.SysNoticeDetailResp;
import com.travis.monolith.system.notice.api.response.SysNoticePageResp;
import com.travis.monolith.system.notice.internal.converter.SysNoticeConverter;
import com.travis.monolith.system.notice.internal.entity.SysNotice;
import com.travis.monolith.system.notice.internal.entity.SysUserMessage;
import com.travis.monolith.system.notice.internal.mapper.SysNoticeMapper;
import com.travis.monolith.system.notice.internal.mapper.SysUserMessageMapper;
import com.travis.monolith.system.notice.internal.service.SysNoticeService;
import com.travis.monolith.system.role.api.SysRoleApi;
import com.travis.monolith.system.user.api.SysUserApi;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice>
        implements SysNoticeService {
    private static final int AUDIENCE_ALL = 0;
    private static final int AUDIENCE_USER = 1;
    private static final int AUDIENCE_ROLE = 2;
    private static final int AUDIENCE_DEPT = 3;

    private final SysUserMessageMapper userMessageMapper;
    private final SysUserApi userApi;
    private final SysRoleApi roleApi;
    private final SysNoticeConverter converter;

    public SysNoticeServiceImpl(
            SysUserMessageMapper userMessageMapper,
            SysUserApi userApi,
            SysRoleApi roleApi,
            SysNoticeConverter converter) {
        this.userMessageMapper = userMessageMapper;
        this.userApi = userApi;
        this.roleApi = roleApi;
        this.converter = converter;
    }

    @Override
    public PageResp<SysNoticePageResp> page(SysNoticePageReq req) {
        var wrapper =
                new LambdaQueryWrapperX<SysNotice>()
                        .likeIfPresent(SysNotice::getTitle, req.getTitle())
                        .eqIfPresent(SysNotice::getNoticeType, req.getNoticeType())
                        .eqIfPresent(SysNotice::getStatus, req.getStatus())
                        .orderByDesc(SysNotice::getCreateTime);
        Page<SysNotice> page = page(new Page<>(req.getPageNum(), req.getPageSize()), wrapper);
        return PageConverter.toResp(page.convert(this::toPageResp));
    }

    @Override
    public SysNoticeDetailResp getDetail(Long id) {
        SysNotice notice = getById(id);
        if (notice == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        return toDetailResp(notice);
    }

    @Override
    @Transactional
    public void create(SysNoticeCreateReq req) {
        validateAudience(req.getAudienceType(), req.getTargetIds());
        var entity = converter.toEntity(req);
        save(entity);
        if (Integer.valueOf(1).equals(entity.getStatus())) {
            publish(entity);
        }
    }

    @Override
    @Transactional
    public void update(Long id, SysNoticeUpdateReq req) {
        validateAudience(req.getAudienceType(), req.getTargetIds());
        var entity = getById(id);
        if (entity == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        converter.update(req, entity);
        updateById(entity);
        if (Integer.valueOf(1).equals(entity.getStatus())) {
            publish(entity);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userMessageMapper.deleteByNoticeId(id);
        removeById(id);
    }

    private void publish(SysNotice notice) {
        if (notice.getPublishTime() == null) {
            notice.setPublishTime(LocalDateTime.now());
            updateById(notice);
        }
        Set<Long> recipientIds = new LinkedHashSet<>(resolveRecipients(notice));
        if (recipientIds.isEmpty()) {
            return;
        }
        Set<Long> existingUserIds =
                userMessageMapper
                        .selectList(
                                new LambdaQueryWrapperX<SysUserMessage>()
                                        .eq(SysUserMessage::getNoticeId, notice.getId())
                                        .in(SysUserMessage::getUserId, recipientIds))
                        .stream()
                        .map(SysUserMessage::getUserId)
                        .collect(Collectors.toSet());
        recipientIds.stream()
                .filter(userId -> !existingUserIds.contains(userId))
                .forEach(
                        userId -> {
                            var message = new SysUserMessage();
                            message.setNoticeId(notice.getId());
                            message.setUserId(userId);
                            message.setReadStatus(0);
                            userMessageMapper.insert(message);
                        });
    }

    private List<Long> resolveRecipients(SysNotice notice) {
        List<Long> targetIds = parseTargetIds(notice.getTargetIds());
        return switch (notice.getAudienceType()) {
            case AUDIENCE_ALL -> userApi.listEnabledUserIds();
            case AUDIENCE_USER -> userApi.listEnabledUserIdsByIds(targetIds);
            case AUDIENCE_ROLE ->
                    userApi.listEnabledUserIdsByIds(roleApi.getUserIdsByRoleIds(targetIds));
            case AUDIENCE_DEPT -> userApi.listEnabledUserIdsByDeptIds(targetIds);
            default -> throw new BizException(CommonErrorCode.BAD_REQUEST);
        };
    }

    private void validateAudience(Integer audienceType, List<Long> targetIds) {
        if (audienceType == null || audienceType < AUDIENCE_ALL || audienceType > AUDIENCE_DEPT) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        if (audienceType != AUDIENCE_ALL && (targetIds == null || targetIds.isEmpty())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
    }

    private SysNoticePageResp toPageResp(SysNotice notice) {
        var response = new SysNoticePageResp();
        BeanUtils.copyProperties(notice, response, "targetIds");
        response.setTargetIds(parseTargetIds(notice.getTargetIds()));
        return response;
    }

    private SysNoticeDetailResp toDetailResp(SysNotice notice) {
        var response = new SysNoticeDetailResp();
        BeanUtils.copyProperties(notice, response, "targetIds");
        response.setTargetIds(parseTargetIds(notice.getTargetIds()));
        return response;
    }

    private List<Long> parseTargetIds(String targetIds) {
        if (targetIds == null || targetIds.isBlank()) {
            return List.of();
        }
        return Arrays.stream(targetIds.split(","))
                .filter(value -> !value.isBlank())
                .map(Long::valueOf)
                .toList();
    }
}
