package com.travis.monolith.system.message.internal.file;

import com.travis.monolith.system.file.api.SysFileReferenceChecker;
import com.travis.monolith.system.message.internal.entity.SysMessage;
import com.travis.monolith.system.message.internal.entity.SysMessageTemplate;
import com.travis.monolith.system.message.internal.service.SysMessageService;
import com.travis.monolith.system.message.internal.service.SysMessageTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 检查文件是否被消息正文引用。 */
@Component
@RequiredArgsConstructor
public class SysMessageFileReferenceChecker implements SysFileReferenceChecker {

    private final SysMessageService messageService;
    private final SysMessageTemplateService messageTemplateService;

    @Override
    public boolean isReferenced(Long fileId) {
        var reference = "data-file-id=\"" + fileId + "\"";
        var singleQuoteReference = "data-file-id='" + fileId + "'";
        boolean referencedByMessage =
                messageService
                        .lambdaQuery()
                        .and(
                                query ->
                                        query.like(SysMessage::getContent, reference)
                                                .or()
                                                .like(SysMessage::getContent, singleQuoteReference))
                        .exists();
        return referencedByMessage
                || messageTemplateService
                        .lambdaQuery()
                        .and(
                                query ->
                                        query.like(SysMessageTemplate::getContent, reference)
                                                .or()
                                                .like(
                                                        SysMessageTemplate::getContent,
                                                        singleQuoteReference))
                        .exists();
    }
}
