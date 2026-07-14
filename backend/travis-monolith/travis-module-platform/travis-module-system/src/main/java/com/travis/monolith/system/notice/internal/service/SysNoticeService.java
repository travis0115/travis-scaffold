package com.travis.monolith.system.notice.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.notice.api.request.SysNoticeCreateReq;
import com.travis.monolith.system.notice.api.request.SysNoticePageReq;
import com.travis.monolith.system.notice.api.request.SysNoticeUpdateReq;
import com.travis.monolith.system.notice.api.response.SysNoticeResp;
import com.travis.monolith.system.notice.internal.entity.SysNotice;

public interface SysNoticeService extends IService<SysNotice> {
    PageResp<SysNoticeResp> page(SysNoticePageReq req);

    PageResp<SysNoticeResp> pagePublished(SysNoticePageReq req);

    SysNoticeResp getOrThrow(Long id);

    void create(SysNoticeCreateReq req);

    void update(Long id, SysNoticeUpdateReq req);

    void updateStatus(Long id, Integer status);

    void updatePinned(Long id, Integer isPinned);

    void delete(Long id);
}
