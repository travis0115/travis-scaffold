package com.travis.monolith.system.file.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigReq;
import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;

public interface SysFileStorageConfigService extends IService<SysFileStorageConfig> {
    void create(SysFileStorageConfigReq req);

    void update(Long id, SysFileStorageConfigReq req);
}
