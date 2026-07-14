package com.travis.monolith.system.message.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.message.api.request.SysMessageTemplateCreateReq;
import com.travis.monolith.system.message.api.request.SysMessageTemplatePageReq;
import com.travis.monolith.system.message.api.request.SysMessageTemplateUpdateReq;
import com.travis.monolith.system.message.api.response.SysMessageTemplateResp;
import com.travis.monolith.system.message.internal.entity.SysMessageTemplate;

/** 消息模板服务。 */
public interface SysMessageTemplateService extends IService<SysMessageTemplate> {
    PageResp<SysMessageTemplateResp> page(SysMessageTemplatePageReq req);

    SysMessageTemplateResp getOrThrow(Long id);

    SysMessageTemplateResp get(Long id);

    void create(SysMessageTemplateCreateReq req);

    void update(Long id, SysMessageTemplateUpdateReq req);

    void delete(Long id);
}
