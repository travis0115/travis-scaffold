package com.travis.monolith.system.notice.internal.api;

import com.travis.monolith.system.notice.api.SysNoticeApi;
import com.travis.monolith.system.notice.api.request.SysNoticeCreateReq;
import com.travis.monolith.system.notice.internal.service.SysNoticeService;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SysNoticeApiImpl implements SysNoticeApi {

    private final SysNoticeService noticeService;

    @Override
    public void publishToUsers(String title, String content, Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        var request = new SysNoticeCreateReq();
        request.setTitle(title);
        request.setContent(content);
        request.setNoticeType(1);
        request.setStatus(1);
        request.setAudienceType(1);
        request.setTargetIds(List.copyOf(userIds));
        request.setPublishTime(LocalDateTime.now());
        request.setRemark("任务调度失败告警");
        noticeService.create(request);
    }
}
