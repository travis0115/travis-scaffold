package com.travis.monolith.system.file.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.file.api.request.SysFilePageReq;
import com.travis.monolith.system.file.api.response.FileUploadResp;
import com.travis.monolith.system.file.api.response.SysFileResp;
import com.travis.monolith.system.file.internal.entity.SysFile;
import java.util.Collection;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件管理服务接口
 *
 * @author travis
 */
public interface SysFileService extends IService<SysFile> {

    /**
     * 上传文件，返回文件相对路径（用于数据库存储）
     *
     * @param file 文件
     * @return 文件相对路径，如 /files/2026-06-02/abc.jpg
     */
    FileUploadResp upload(MultipartFile file, Long folderId, String uploaderType, Long uploaderId);

    /** 分页查询文件。 */
    PageResp<SysFileResp> page(SysFilePageReq req);

    /**
     * 根据文件ID拼接完整访问URL
     *
     * @param fileId 文件ID
     * @return 完整访问URL
     */
    String getFileUrlById(Long fileId);

    /** 批量查询文件访问地址。 */
    Map<Long, String> getFileUrlMapByIds(Collection<Long> fileIds);

    /** 删除文件元数据，并在提交后清理存储对象。 */
    void deleteById(Long id);

    /** 清理已删除文件对应的存储对象。 */
    void deleteStorageObject(String storageType, String storagePath, String path);
}
