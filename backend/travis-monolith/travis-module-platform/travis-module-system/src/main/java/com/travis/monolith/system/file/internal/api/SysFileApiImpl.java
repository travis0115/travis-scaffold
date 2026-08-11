package com.travis.monolith.system.file.internal.api;

import com.travis.monolith.system.file.api.SysFileApi;
import com.travis.monolith.system.file.api.response.FileUploadResp;
import com.travis.monolith.system.file.internal.service.RichTextFileReferenceService;
import com.travis.monolith.system.file.internal.service.SysFileService;
import java.util.Collection;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件模块对外 API 实现，委托调用内部 Service
 *
 * @author travis
 */
@Component
@RequiredArgsConstructor
public class SysFileApiImpl implements SysFileApi {

    private final SysFileService fileService;
    private final RichTextFileReferenceService richTextFileReferenceService;

    @Override
    public FileUploadResp upload(
            MultipartFile file, Long folderId, String uploaderType, Long uploaderId) {
        return fileService.upload(file, folderId, uploaderType, uploaderId);
    }

    @Override
    public String getFileUrlById(Long fileId) {
        return fileService.getFileUrlById(fileId);
    }

    @Override
    public Map<Long, String> getFileUrlMapByIds(Collection<Long> fileIds) {
        return fileService.getFileUrlMapByIds(fileIds);
    }

    @Override
    public String stripManagedImageSources(String html) {
        return richTextFileReferenceService.stripManagedImageSources(html);
    }

    @Override
    public String resolveManagedImageSources(String html) {
        return richTextFileReferenceService.resolveManagedImageSources(html);
    }
}
