package com.travis.monolith.system.file.api;

/**
 * 文件模块对外 API，供跨模块调用，只暴露 DTO，不暴露 entity
 *
 * @author travis
 */
public interface SysFileApi {

    /**
     * 根据文件ID拼接完整访问URL
     *
     * @param fileId 文件ID
     * @return 完整访问URL
     */
    String getFileUrlById(Long fileId);

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
