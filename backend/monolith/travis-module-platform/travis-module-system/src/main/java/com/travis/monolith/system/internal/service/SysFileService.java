package com.travis.monolith.system.internal.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件管理服务接口
 *
 * @author travis
 */
public interface SysFileService {

    /**
     * 上传文件，返回文件访问URL
     *
     * @param file 文件
     * @return 文件访问URL
     */
    String upload(MultipartFile file);
}
