package com.travis.monolith.system.message.internal.api;

import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.framework.web.core.xss.HtmlSanitizer;
import com.travis.monolith.system.message.api.SysMessageApi;
import com.travis.monolith.system.message.api.enums.SysMessageChannel;
import com.travis.monolith.system.message.api.enums.SysMessagePushType;
import com.travis.monolith.system.message.api.enums.SysMessageReceiverScope;
import com.travis.monolith.system.message.api.request.SysMessageCreateReq;
import com.travis.monolith.system.message.api.request.SysSourceMessagePublishReq;
import com.travis.monolith.system.message.internal.service.SysMessageService;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 消息推送对外 API 默认实现。 */
@Component
@RequiredArgsConstructor
public class SysMessageApiImpl implements SysMessageApi {
    private final SysMessageService messageService;
    private final HtmlSanitizer htmlSanitizer;

    @Override
    @Transactional
    public void publishToUsers(
            String receiverType, String title, String content, Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        String sanitizedContent = htmlSanitizer.sanitize(content);
        if (!HtmlSanitizer.hasContent(sanitizedContent)) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "消息内容清洗后不能为空");
        }
        if (sanitizedContent.length() > 5000) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "消息内容清洗后长度不能超过5000个字符");
        }
        var request = new SysMessageCreateReq();
        request.setTitle(title);
        request.setContent(sanitizedContent);
        request.setPushType(SysMessagePushType.MANUAL.getValue());
        request.setChannel(SysMessageChannel.IN_APP.getValue());
        request.setReceiverType(receiverType);
        request.setReceiverScope(SysMessageReceiverScope.USER.getValue());
        request.setReceiverValues(List.copyOf(userIds));
        Long messageId = messageService.createSystem(request);
        messageService.pushAutomatic(messageId);
    }

    @Override
    public void publishSourceMessage(SysSourceMessagePublishReq req) {
        messageService.publishSourceMessage(req);
    }

    @Override
    public void revokeSourceMessage(String sourceType, String sourceId, String receiverType) {
        messageService.revokeSourceMessage(sourceType, sourceId, receiverType);
    }

    @Override
    public void deleteSourceMessage(String sourceType, String sourceId, String receiverType) {
        messageService.deleteSourceMessage(sourceType, sourceId, receiverType);
    }
}
