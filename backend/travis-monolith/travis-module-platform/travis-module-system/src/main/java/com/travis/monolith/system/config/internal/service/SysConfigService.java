package com.travis.monolith.system.config.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.config.api.request.SysConfigPageReq;
import com.travis.monolith.system.config.api.request.SysConfigReq;
import com.travis.monolith.system.config.api.response.SysConfigResp;
import com.travis.monolith.system.config.internal.entity.SysConfig;

/**
 * 系统配置服务接口
 *
 * @author travis
 */
public interface SysConfigService extends IService<SysConfig> {

    /** 分页查询系统配置 */
    PageResp<SysConfigResp> page(SysConfigPageReq req);

    /** 获取配置详情 */
    SysConfigResp getById(Long id);

    /** 根据配置键获取配置值 */
    String getValue(String configKey);

    /** 新增配置 */
    void create(SysConfigReq req);

    /** 更新配置 */
    void update(Long id, SysConfigReq req);

    /** 删除配置 */
    void deleteById(Long id);
}
