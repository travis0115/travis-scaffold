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
    /** 分页查询消息模板。 */
    PageResp<SysMessageTemplateResp> page(SysMessageTemplatePageReq req);

    /** 查询消息模板，不存在时抛出业务异常。 */
    SysMessageTemplateResp getOrThrow(Long id);

    /** 查询消息模板，不存在时返回 {@code null}。 */
    SysMessageTemplateResp get(Long id);

    /** 创建消息模板。 */
    void create(SysMessageTemplateCreateReq req);

    /** 更新消息模板。 */
    void update(Long id, SysMessageTemplateUpdateReq req);

    /** 删除消息模板。 */
    void delete(Long id);
}
