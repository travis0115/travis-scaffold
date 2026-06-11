package com.travis.monolith.system.file.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.file.api.request.SysFilePageReq;
import com.travis.monolith.system.file.api.response.FileUploadResp;
import com.travis.monolith.system.file.api.response.SysFileResp;
import com.travis.monolith.system.file.internal.entity.SysFile;
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
    FileUploadResp upload(MultipartFile file, Long folderId);

    PageResp<SysFileResp> page(SysFilePageReq req);

    /**
     * 根据相对路径拼接完整访问URL
     *
     * @param path 文件相对路径
     * @return 完整访问URL，如 http://127.0.0.1/files/2026-06-02/abc.jpg
     */
    String getFileUrl(String path);
}
