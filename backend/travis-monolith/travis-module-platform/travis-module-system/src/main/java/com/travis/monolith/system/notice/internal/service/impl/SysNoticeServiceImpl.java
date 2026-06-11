package com.travis.monolith.system.notice.internal.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.web.core.exception.BizException;
import com.travis.monolith.system.notice.api.request.SysNoticePageReq;
import com.travis.monolith.system.notice.api.request.SysNoticeReq;
import com.travis.monolith.system.notice.api.response.SysNoticeResp;
import com.travis.monolith.system.notice.internal.entity.SysNotice;
import com.travis.monolith.system.notice.internal.entity.SysUserMessage;
import com.travis.monolith.system.notice.internal.mapper.SysNoticeMapper;
import com.travis.monolith.system.notice.internal.mapper.SysUserMessageMapper;
import com.travis.monolith.system.notice.internal.service.SysNoticeService;
import com.travis.monolith.system.role.api.SysRoleApi;
import com.travis.monolith.system.user.api.SysUserApi;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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

    public SysNoticeServiceImpl(
            SysUserMessageMapper userMessageMapper, SysUserApi userApi, SysRoleApi roleApi) {
        this.userMessageMapper = userMessageMapper;
        this.userApi = userApi;
        this.roleApi = roleApi;
    }

    @Override
    public PageResp<SysNoticeResp> page(SysNoticePageReq req) {
        var wrapper =
                new LambdaQueryWrapperX<SysNotice>()
                        .likeIfPresent(SysNotice::getTitle, req.getTitle())
                        .eqIfPresent(SysNotice::getNoticeType, req.getNoticeType())
                        .eqIfPresent(SysNotice::getStatus, req.getStatus())
                        .orderByDesc(SysNotice::getCreateTime);
        Page<SysNotice> page = page(new Page<>(req.getPageNum(), req.getPageSize()), wrapper);
        return PageConverter.toResp(page.convert(this::toResp));
    }

    @Override
    public SysNoticeResp getDetail(Long id) {
        SysNotice notice = getById(id);
        if (notice == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        return toResp(notice);
    }

    @Override
    @Transactional
    public void create(SysNoticeReq req) {
        validateAudience(req);
        var entity = new SysNotice();
        copyRequest(req, entity);
        save(entity);
        if (Integer.valueOf(1).equals(entity.getStatus())) {
            publish(entity);
        }
    }

    @Override
    @Transactional
    public void update(Long id, SysNoticeReq req) {
        validateAudience(req);
        var entity = getById(id);
        if (entity == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        copyRequest(req, entity);
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

    private void validateAudience(SysNoticeReq req) {
        Integer audienceType = req.getAudienceType();
        if (audienceType == null || audienceType < AUDIENCE_ALL || audienceType > AUDIENCE_DEPT) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
        if (audienceType != AUDIENCE_ALL
                && (req.getTargetIds() == null || req.getTargetIds().isEmpty())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST);
        }
    }

    private void copyRequest(SysNoticeReq req, SysNotice entity) {
        BeanUtils.copyProperties(req, entity, "targetIds");
        entity.setTargetIds(serializeTargetIds(req.getTargetIds()));
    }

    private SysNoticeResp toResp(SysNotice notice) {
        var response = new SysNoticeResp();
        BeanUtils.copyProperties(notice, response, "targetIds");
        response.setTargetIds(parseTargetIds(notice.getTargetIds()));
        return response;
    }

    private String serializeTargetIds(Collection<Long> targetIds) {
        if (targetIds == null || targetIds.isEmpty()) {
            return null;
        }
        return targetIds.stream().map(String::valueOf).collect(Collectors.joining(","));
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
