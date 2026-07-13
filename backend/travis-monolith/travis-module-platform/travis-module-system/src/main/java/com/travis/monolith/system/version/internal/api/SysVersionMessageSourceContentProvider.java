package com.travis.monolith.system.version.internal.api;

import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.monolith.system.common.api.enums.PublishStatus;
import com.travis.monolith.system.message.api.SysMessageSourceContentProvider;
import com.travis.monolith.system.message.api.enums.SysMessageSourceType;
import com.travis.monolith.system.message.api.response.SysMessageSourceContentResp;
import com.travis.monolith.system.version.internal.service.SysVersionService;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 版本更新消息内容解析器。 */
@Component
@RequiredArgsConstructor
public class SysVersionMessageSourceContentProvider implements SysMessageSourceContentProvider {
    private final SysVersionService versionService;

    @Override
    public String getSourceType() {
        return SysMessageSourceType.VERSION.getValue();
    }

    @Override
    public SysMessageSourceContentResp get(String sourceId) {
        var version = versionService.getById(parseId(sourceId));
        if (!PublishStatus.PUBLISHED.getValue().equals(version.getStatus())
                || version.getPublishTime() == null
                || version.getPublishTime().isAfter(LocalDateTime.now())) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        var resp = new SysMessageSourceContentResp();
        resp.setTitle(version.getTitle());
        resp.setContent(version.getContent());
        resp.setPublishTime(version.getPublishTime());
        resp.setMetadata(Map.of("version", version.getVersion()));
        return resp;
    }

    private Long parseId(String sourceId) {
        try {
            return Long.valueOf(sourceId);
        } catch (NumberFormatException ex) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
    }
}
