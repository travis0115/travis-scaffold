package com.travis.monolith.system.file.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigCreateReq;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigPageReq;
import com.travis.monolith.system.file.api.request.SysFileStorageConfigUpdateReq;
import com.travis.monolith.system.file.api.response.SysFileStorageConfigResp;
import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import java.util.List;

/** 文件存储配置管理服务。 */
public interface SysFileStorageConfigService extends IService<SysFileStorageConfig> {
    /** 分页查询存储配置。 */
    PageResp<SysFileStorageConfigResp> page(SysFileStorageConfigPageReq req);

    /** 查询全部存储配置。 */
    List<SysFileStorageConfigResp> listAll();

    /** 查询存储配置，配置不存在时抛出业务异常。 */
    SysFileStorageConfigResp getOrThrow(Long id);

    /** 查询默认存储配置，未配置时抛出业务异常。 */
    SysFileStorageConfigResp getDefaultOrThrow();

    /** 查询已启用的本地存储配置。 */
    List<SysFileStorageConfig> listEnabledLocalConfigs();

    /** 创建存储配置。 */
    void create(SysFileStorageConfigCreateReq req);

    /** 更新存储配置。 */
    void update(Long id, SysFileStorageConfigUpdateReq req);

    /** 更新存储配置状态。 */
    void updateStatus(Long id, Integer status);

    /** 将指定配置设为默认存储配置。 */
    void setDefault(Long id);

    /** 删除存储配置。 */
    void deleteById(Long id);
}
