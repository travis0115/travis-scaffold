package com.travis.monolith.system.file.internal.service;

import com.travis.monolith.system.file.api.response.SysFileStorageConfigResp;
import com.travis.monolith.system.file.internal.entity.SysFile;

/** 文件元数据写入服务。 */
public interface SysFileMetadataService {

    /** 校验目标文件夹并保存文件元数据。 */
    void save(SysFile file, SysFileStorageConfigResp configSnapshot);
}
