package com.travis.monolith.system.file.api;

import java.util.Collection;
import java.util.Map;

/** 文件上传主体名称解析扩展点，由对应主体模块实现。 */
public interface SysFileUploaderNameResolver {

    /** 支持的上传主体类型。 */
    String getUploaderType();

    /** 批量解析上传主体 ID 与展示名称。 */
    Map<Long, String> resolveNames(Collection<Long> uploaderIds);
}
