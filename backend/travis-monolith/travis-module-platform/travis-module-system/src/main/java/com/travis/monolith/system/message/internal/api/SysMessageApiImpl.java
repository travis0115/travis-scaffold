package com.travis.monolith.system.message.internal.api;

import com.travis.monolith.system.message.api.SysMessageApi;
import com.travis.monolith.system.message.api.request.SysMessageCreateReq;
import com.travis.monolith.system.message.internal.service.SysMessageService;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SysMessageApiImpl implements SysMessageApi {

    private final SysMessageService messageService;

    @Override
    public void publishToUsers(String title, String content, Collection<Long> userIds) {
        publishToUsers(title, content, userIds, null, null);
    }

    @Override
    public void publishToUsers(
            String title,
            String content,
            Collection<Long> userIds,
            String sourceType,
            String sourceId) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        var request = new SysMessageCreateReq();
        request.setTitle(title);
        request.setContent(content);
        request.setMessageType(1);
        request.setSourceType(sourceType);
        request.setSourceId(sourceId);
        request.setChannels("IN_APP");
        request.setStatus(1);
        request.setAudienceType(1);
        request.setTargetIds(List.copyOf(userIds));
        request.setPublishTime(LocalDateTime.now());
        messageService.create(request);
    }
}
