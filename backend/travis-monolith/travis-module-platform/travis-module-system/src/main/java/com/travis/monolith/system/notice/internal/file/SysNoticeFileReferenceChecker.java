package com.travis.monolith.system.notice.internal.file;

import com.travis.monolith.system.file.api.SysFileReferenceChecker;
import com.travis.monolith.system.notice.internal.entity.SysNotice;
import com.travis.monolith.system.notice.internal.service.SysNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 检查文件是否被公告正文引用。 */
@Component
@RequiredArgsConstructor
public class SysNoticeFileReferenceChecker implements SysFileReferenceChecker {

    private final SysNoticeService noticeService;

    @Override
    public boolean isReferenced(Long fileId) {
        var reference = "data-file-id=\"" + fileId + "\"";
        var singleQuoteReference = "data-file-id='" + fileId + "'";
        return noticeService
                .lambdaQuery()
                .and(
                        query ->
                                query.like(SysNotice::getContent, reference)
                                        .or()
                                        .like(SysNotice::getContent, singleQuoteReference))
                .exists();
    }
}
