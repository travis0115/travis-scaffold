package com.travis.monolith.system.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.framework.web.core.model.PageResult;
import com.travis.monolith.system.internal.model.entity.SysConfig;
import com.travis.monolith.system.internal.model.req.SysConfigPageReq;
import com.travis.monolith.system.internal.model.req.SysConfigReq;
import com.travis.monolith.system.internal.model.resp.SysConfigResp;

/**
 * 系统配置服务接口
 *
 * @author travis
 */
public interface SysConfigService extends IService<SysConfig> {

    /**
     * 分页查询系统配置
     */
    PageResult<SysConfigResp> getConfigPage(SysConfigPageReq req);

    /**
     * 获取配置详情
     */
    SysConfigResp getConfigDetail(Long id);

    /**
     * 根据配置键获取配置值
     */
    String getConfigValue(String configKey);

    /**
     * 新增配置
     */
    void addConfig(SysConfigReq req);

    /**
     * 更新配置
     */
    void updateConfig(Long id, SysConfigReq req);

    /**
     * 删除配置
     */
    void deleteConfig(Long id);
}
