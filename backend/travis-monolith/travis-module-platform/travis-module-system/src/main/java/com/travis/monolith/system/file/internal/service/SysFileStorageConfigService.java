package com.travis.monolith.system.file.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigCreateReq;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigPageReq;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigUpdateReq;
import com.travis.monolith.system.file.api.response.SysFileStorageConfigResp;
import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import java.util.List;

public interface SysFileStorageConfigService extends IService<SysFileStorageConfig> {
    PageResp<SysFileStorageConfigResp> page(SysFileStorageConfigPageReq req);

    List<SysFileStorageConfigResp> listAll();

    SysFileStorageConfigResp getOrThrow(Long id);

    SysFileStorageConfigResp getDefaultOrThrow();

    List<SysFileStorageConfig> listEnabledLocalConfigs();

    void create(SysFileStorageConfigCreateReq req);

    void update(Long id, SysFileStorageConfigUpdateReq req);

    void updateStatus(Long id, Integer status);

    void setDefault(Long id);

    void deleteById(Long id);
}
