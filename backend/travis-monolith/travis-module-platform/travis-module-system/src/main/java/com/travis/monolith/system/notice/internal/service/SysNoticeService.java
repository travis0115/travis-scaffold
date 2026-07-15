package com.travis.monolith.system.notice.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.notice.api.request.SysNoticeCreateReq;
import com.travis.monolith.system.notice.api.request.SysNoticePageReq;
import com.travis.monolith.system.notice.api.request.SysNoticeUpdateReq;
import com.travis.monolith.system.notice.api.response.SysNoticeResp;
import com.travis.monolith.system.notice.internal.entity.SysNotice;

/** 公告管理服务。 */
public interface SysNoticeService extends IService<SysNotice> {
    /** 分页查询全部公告。 */
    PageResp<SysNoticeResp> page(SysNoticePageReq req);

    /** 分页查询当前已发布公告。 */
    PageResp<SysNoticeResp> pagePublished(SysNoticePageReq req);

    /** 查询公告详情，公告不存在时抛出业务异常。 */
    SysNoticeResp getOrThrow(Long id);

    /** 创建公告。 */
    void create(SysNoticeCreateReq req);

    /** 更新公告内容。 */
    void update(Long id, SysNoticeUpdateReq req);

    /** 更新公告发布状态。 */
    void updateStatus(Long id, Integer status);

    /** 更新公告置顶状态。 */
    void updatePinned(Long id, Integer isPinned);

    /** 删除公告。 */
    void delete(Long id);
}
