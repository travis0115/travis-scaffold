package com.travis.monolith.system.file.api;

import com.travis.monolith.system.file.api.response.FileUploadResp;
import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件模块对外 API，供跨模块调用，只暴露 DTO，不暴露 entity
 *
 * @author travis
 */
@Validated
public interface SysFileApi {

    /**
     * 上传文件。
     *
     * @param file 文件
     * @param folderId 文件夹ID
     * @param uploaderType 上传主体类型
     * @param uploaderId 上传主体ID
     * @return 文件上传结果
     */
    FileUploadResp upload(
            @NotNull(message = "上传文件不能为空") MultipartFile file,
            Long folderId,
            String uploaderType,
            Long uploaderId);

    /**
     * 根据文件ID拼接完整访问URL
     *
     * @param fileId 文件ID
     * @return 完整访问URL
     */
    String getFileUrlById(Long fileId);

    /** 批量查询文件访问地址。 */
    Map<Long, String> getFileUrlMapByIds(Collection<Long> fileIds);

    /**
     * 移除富文本中系统文件图片的访问地址，保留文件ID引用
     *
     * @param html 富文本内容
     * @return 处理后的富文本内容
     */
    String stripManagedImageSources(String html);

    /**
     * 为富文本中系统文件图片补充当前访问地址
     *
     * @param html 富文本内容
     * @return 处理后的富文本内容
     */
    String resolveManagedImageSources(String html);
}
