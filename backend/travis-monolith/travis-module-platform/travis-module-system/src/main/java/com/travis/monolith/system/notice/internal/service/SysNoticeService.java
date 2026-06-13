package com.travis.monolith.system.notice.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.notice.api.request.SysNoticeCreateReq;
import com.travis.monolith.system.notice.api.request.SysNoticePageReq;
import com.travis.monolith.system.notice.api.request.SysNoticeUpdateReq;
import com.travis.monolith.system.notice.api.response.SysNoticeDetailResp;
import com.travis.monolith.system.notice.api.response.SysNoticePageResp;
import com.travis.monolith.system.notice.internal.entity.SysNotice;

public interface SysNoticeService extends IService<SysNotice> {
    PageResp<SysNoticePageResp> page(SysNoticePageReq req);

    SysNoticeDetailResp getDetail(Long id);

    void create(SysNoticeCreateReq req);

    void update(Long id, SysNoticeUpdateReq req);

    void delete(Long id);
}
