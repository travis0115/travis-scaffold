package com.travis.monolith.system.notice.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.notice.api.request.SysNoticePageReq;
import com.travis.monolith.system.notice.api.request.SysNoticeReq;
import com.travis.monolith.system.notice.api.response.SysNoticeResp;
import com.travis.monolith.system.notice.internal.entity.SysNotice;

public interface SysNoticeService extends IService<SysNotice> {
    PageResp<SysNoticeResp> page(SysNoticePageReq req);

    SysNoticeResp getDetail(Long id);

    void create(SysNoticeReq req);

    void update(Long id, SysNoticeReq req);

    void delete(Long id);
}
