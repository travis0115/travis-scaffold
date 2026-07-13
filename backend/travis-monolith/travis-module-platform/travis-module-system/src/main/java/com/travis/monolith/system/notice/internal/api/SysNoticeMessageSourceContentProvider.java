package com.travis.monolith.system.notice.internal.api;

import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.monolith.system.common.api.enums.PublishStatus;
import com.travis.monolith.system.message.api.SysMessageSourceContentProvider;
import com.travis.monolith.system.message.api.enums.SysMessageSourceType;
import com.travis.monolith.system.message.api.response.SysMessageSourceContentResp;
import com.travis.monolith.system.notice.internal.service.SysNoticeService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 系统公告消息内容解析器。 */
@Component
@RequiredArgsConstructor
public class SysNoticeMessageSourceContentProvider implements SysMessageSourceContentProvider {
    private final SysNoticeService noticeService;

    @Override
    public String getSourceType() {
        return SysMessageSourceType.NOTICE.getValue();
    }

    @Override
    public SysMessageSourceContentResp get(String sourceId) {
        var notice = noticeService.get(parseId(sourceId));
        if (!PublishStatus.PUBLISHED.getValue().equals(notice.getStatus())
                || notice.getPublishTime() == null
                || notice.getPublishTime().isAfter(LocalDateTime.now())) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        var resp = new SysMessageSourceContentResp();
        resp.setTitle(notice.getTitle());
        resp.setContent(notice.getContent());
        resp.setPublishTime(notice.getPublishTime());
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
