package com.travis.monolith.system.file.internal.service;

import java.util.List;

/** 富文本中的系统文件图片引用处理服务。 */
public interface RichTextFileReferenceService {

    /** 移除系统文件图片的临时访问地址，仅保留文件 ID 引用。 */
    String stripManagedImageSources(String html);

    /** 根据文件 ID 将系统文件图片补充为当前可访问地址。 */
    String resolveManagedImageSources(String html);

    /** 批量补充富文本中的文件访问地址，全部内容共用一次文件查询。 */
    List<String> resolveManagedImageSources(List<String> htmlList);
}
