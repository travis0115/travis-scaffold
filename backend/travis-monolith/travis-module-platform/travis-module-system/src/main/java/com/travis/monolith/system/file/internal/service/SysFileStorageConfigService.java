package com.travis.monolith.system.file.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigCreateReq;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigUpdateReq;
import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import java.util.List;

public interface SysFileStorageConfigService extends IService<SysFileStorageConfig> {
    List<SysFileStorageConfig> listAll();

    void create(SysFileStorageConfigCreateReq req);

    void update(Long id, SysFileStorageConfigUpdateReq req);

    void updateStatus(Long id, Integer status);
}
