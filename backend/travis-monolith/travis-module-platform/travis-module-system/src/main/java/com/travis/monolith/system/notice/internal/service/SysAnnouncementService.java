package com.travis.monolith.system.notice.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.notice.api.request.SysAnnouncementCreateReq;
import com.travis.monolith.system.notice.api.request.SysAnnouncementPageReq;
import com.travis.monolith.system.notice.api.request.SysAnnouncementUpdateReq;
import com.travis.monolith.system.notice.api.response.SysAnnouncementResp;
import com.travis.monolith.system.notice.internal.entity.SysAnnouncement;

public interface SysAnnouncementService extends IService<SysAnnouncement> {
    PageResp<SysAnnouncementResp> page(SysAnnouncementPageReq req);

    SysAnnouncementResp get(Long id);

    void create(SysAnnouncementCreateReq req);

    void update(Long id, SysAnnouncementUpdateReq req);

    void updateStatus(Long id, Integer status);

    void delete(Long id);
}
