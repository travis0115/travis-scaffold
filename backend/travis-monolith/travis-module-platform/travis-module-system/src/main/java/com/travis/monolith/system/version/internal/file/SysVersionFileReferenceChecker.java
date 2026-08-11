package com.travis.monolith.system.version.internal.file;

import com.travis.monolith.system.file.api.ManagedFileReferenceParser;
import com.travis.monolith.system.file.api.SysFileReferenceChecker;
import com.travis.monolith.system.version.internal.entity.SysVersion;
import com.travis.monolith.system.version.internal.service.SysVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 检查文件是否被版本日志正文引用。 */
@Component
@RequiredArgsConstructor
public class SysVersionFileReferenceChecker implements SysFileReferenceChecker {

    private final SysVersionService versionService;

    @Override
    public boolean isReferenced(Long fileId) {
        return versionService
                .lambdaQuery()
                .select(SysVersion::getContent)
                .like(SysVersion::getContent, fileId.toString())
                .list()
                .stream()
                .anyMatch(
                        item ->
                                ManagedFileReferenceParser.containsFileId(
                                        item.getContent(), fileId));
    }
}
