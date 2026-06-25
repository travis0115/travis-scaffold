package com.travis.monolith.system.message.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.message.api.request.SysMessageCreateReq;
import com.travis.monolith.system.message.api.request.SysMessagePageReq;
import com.travis.monolith.system.message.api.request.SysMessageUpdateReq;
import com.travis.monolith.system.message.api.response.SysMessageDetailResp;
import com.travis.monolith.system.message.api.response.SysMessagePageResp;
import com.travis.monolith.system.message.internal.entity.SysMessage;

public interface SysMessageService extends IService<SysMessage> {
    PageResp<SysMessagePageResp> page(SysMessagePageReq req);

    SysMessageDetailResp get(Long id);

    void create(SysMessageCreateReq req);

    void update(Long id, SysMessageUpdateReq req);

    void updateStatus(Long id, Integer status);

    void delete(Long id);
}
