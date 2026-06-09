package com.travis.monolith.system.file.api;

/**
 * 文件模块对外 API，供跨模块调用，只暴露 DTO，不暴露 entity
 *
 * @author travis
 */
public interface SysFileApi {

    /**
     * 根据相对路径拼接完整访问URL
     *
     * @param path 文件相对路径
     * @return 完整访问URL
     */
    String getFileUrl(String path);
}
