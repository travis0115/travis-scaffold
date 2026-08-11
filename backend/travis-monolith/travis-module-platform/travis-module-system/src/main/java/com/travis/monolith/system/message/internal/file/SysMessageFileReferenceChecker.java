package com.travis.monolith.system.message.internal.file;

import com.travis.monolith.system.file.api.ManagedFileReferenceParser;
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
        boolean referencedByMessage =
                messageService
                        .lambdaQuery()
                        .select(SysMessage::getContent)
                        .like(SysMessage::getContent, fileId.toString())
                        .list()
                        .stream()
                        .anyMatch(
                                item ->
                                        ManagedFileReferenceParser.containsFileId(
                                                item.getContent(), fileId));
        return referencedByMessage
                || messageTemplateService
                        .lambdaQuery()
                        .select(SysMessageTemplate::getContent)
                        .like(SysMessageTemplate::getContent, fileId.toString())
                        .list()
                        .stream()
                        .anyMatch(
                                item ->
                                        ManagedFileReferenceParser.containsFileId(
                                                item.getContent(), fileId));
    }
}
