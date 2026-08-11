package com.travis.monolith.system.version.internal.service.impl;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.constant.LoginType;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageRequest;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.infrastructure.framework.redis.core.annotation.DistributedLock;
import com.travis.monolith.system.common.api.enums.PublishStatus;
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import com.travis.monolith.system.file.api.SysFileApi;
import com.travis.monolith.system.message.api.SysMessageApi;
import com.travis.monolith.system.message.api.enums.SysMessageReceiverScope;
import com.travis.monolith.system.message.api.enums.SysMessageSourceType;
import com.travis.monolith.system.message.api.enums.SysMessageType;
import com.travis.monolith.system.message.api.request.SysSourceMessagePublishReq;
import com.travis.monolith.system.version.api.request.SysVersionCreateReq;
import com.travis.monolith.system.version.api.request.SysVersionPageReq;
import com.travis.monolith.system.version.api.request.SysVersionUpdateReq;
import com.travis.monolith.system.version.api.response.SysVersionResp;
import com.travis.monolith.system.version.internal.cache.SysVersionDetailCache;
import com.travis.monolith.system.version.internal.converter.SysVersionConverter;
import com.travis.monolith.system.version.internal.entity.SysVersion;
import com.travis.monolith.system.version.internal.mapper.SysVersionMapper;
import com.travis.monolith.system.version.internal.service.SysVersionService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统版本日志服务实现
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "system:version")
public class SysVersionServiceImpl extends ServiceImplX<SysVersionMapper, SysVersion>
        implements SysVersionService {

    private final SysVersionConverter converter;

    private final SysFileApi fileApi;

    private final SysMessageApi messageApi;
    private final SysVersionDetailCache detailCache;

    private static final Map<String, SFunction<SysVersion, ?>> SORT_COLUMNS =
            Map.of(
                    "publishTime",
                    SysVersion::getPublishTime,
                    "createTime",
                    SysVersion::getCreateTime);

    /** 分页查询版本。 */
    @Override
    public PageResp<SysVersionResp> page(SysVersionPageReq req) {
        var wrapper =
                new LambdaQueryWrapperX<SysVersion>()
                        .likeIfPresent(SysVersion::getVersion, req.getVersion())
                        .likeIfPresent(SysVersion::getTitle, req.getTitle())
                        .eqIfPresent(SysVersion::getStatus, req.getStatus())
                        .geIfPresent(
                                SysVersion::getPublishTime,
                                req.getPublishStartDate() == null
                                        ? null
                                        : req.getPublishStartDate().atStartOfDay())
                        .ltIfPresent(
                                SysVersion::getPublishTime,
                                req.getPublishEndDate() == null
                                        ? null
                                        : req.getPublishEndDate().plusDays(1).atStartOfDay())
                        .orderByAllowed(
                                req.getOrderBy(),
                                req.getAsc(),
                                SORT_COLUMNS,
                                false,
                                SysVersion::getCreateTime);
        var page = page(req.getPageNum(), req.getPageSize(), wrapper);
        var response = PageConverter.toResp(page.convert(converter::toResp));
        resolveContents(response.getRecords());
        return response;
    }

    /** 查询指定版本详情，不存在时抛出业务异常。 */
    @Override
    public SysVersionResp getDetailByIdOrThrow(Long id) {
        var response = converter.toResp(detailCache.getOrThrow(id));
        resolveContents(List.of(response));
        return response;
    }

    /** 创建版本。 */
    @Override
    @Transactional
    @DistributedLock(namespace = "system-file-reference", key = "'mutation'", waitTime = 5000)
    public void create(SysVersionCreateReq req) {
        if (PublishStatus.REVOKED.getValue().equals(req.getStatus())) {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "新建版本不能设置为已撤回");
        }
        var count =
                count(
                        new LambdaQueryWrapperX<SysVersion>()
                                .eq(SysVersion::getVersion, req.getVersion()));
        if (count > 0) {
            throw new BizException(SystemErrorCode.VERSION_EXISTS);
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

    /** 更新指定版本。 */
    @Override
    @Transactional
    @DistributedLock(namespace = "system-file-reference", key = "'mutation'", waitTime = 5000)
    @CacheEvict(key = "'detail:'+#id")
    public void update(Long id, SysVersionUpdateReq req) {
        var entity = getByIdOrThrow(id);
        var count =
                count(
                        new LambdaQueryWrapperX<SysVersion>()
                                .eq(SysVersion::getVersion, req.getVersion())
                                .ne(SysVersion::getId, id));
        if (count > 0) {
            throw new BizException(SystemErrorCode.VERSION_EXISTS);
        }
        var normalizedContent = fileApi.stripManagedImageSources(req.getContent());
        converter.update(req, entity);
        entity.setLockVersion(req.getLockVersion());
        entity.setContent(normalizedContent);
        updateOrThrow(entity);
        if (PublishStatus.PUBLISHED.getValue().equals(entity.getStatus())) {
            messageApi.publishSourceMessage(toMessageRequest(entity));
        }
    }

    /** 更新指定版本的状态。 */
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
            updateOrThrow(entity);
            messageApi.publishSourceMessage(toMessageRequest(entity));
        } else {
            throw new BizException(CommonErrorCode.BAD_REQUEST, "版本发布状态流转不合法");
        }
    }

    /** 分页查询已发布的系统公告。 */
    @Override
    public PageResp<SysVersionResp> pagePublished(PageRequest req) {
        var wrapper = new LambdaQueryWrapperX<SysVersion>();
        wrapper.eq(SysVersion::getStatus, PublishStatus.PUBLISHED.getValue())
                .le(SysVersion::getPublishTime, LocalDateTime.now())
                .orderByDesc(SysVersion::getPublishTime)
                .orderByDesc(SysVersion::getCreateTime);
        var page = page(req.getPageNum(), req.getPageSize(), wrapper);
        var response = PageConverter.toResp(page.convert(converter::toResp));
        resolveContents(response.getRecords());
        return response;
    }

    /** 根据 ID 删除指定版本。 */
    @Override
    @Transactional
    @CacheEvict(key = "'detail:'+#id")
    public void deleteById(Long id) {
        getByIdOrThrow(id);
        messageApi.deleteSourceMessage(
                SysMessageSourceType.VERSION.getValue(), id.toString(), LoginType.ADMIN);
        removeById(id);
    }

    /** 将版本转换为消息发布请求。 */
    private SysSourceMessagePublishReq toMessageRequest(SysVersion version) {
        var req = new SysSourceMessagePublishReq();
        req.setMessageType(SysMessageType.VERSION.getValue());
        req.setSourceType(SysMessageSourceType.VERSION.getValue());
        req.setSourceId(version.getId().toString());
        req.setTitle(version.getTitle());
        req.setReceiverType(LoginType.ADMIN);
        req.setReceiverScope(SysMessageReceiverScope.ALL.getValue());
        req.setPublishTime(version.getPublishTime());
        return req;
    }

    /** 批量补充版本正文中的动态文件访问地址。 */
    private void resolveContents(List<SysVersionResp> records) {
        var contents =
                fileApi.resolveManagedImageSources(
                        records.stream().map(SysVersionResp::getContent).toList());
        for (int index = 0; index < records.size(); index++) {
            records.get(index).setContent(contents.get(index));
        }
    }

    /** 更新版本信息，检测并发覆盖。 */
    private void updateOrThrow(SysVersion entity) {
        if (!updateById(entity)) {
            throw new BizException(SystemErrorCode.VERSION_CONCURRENT_UPDATE);
        }
    }
}
