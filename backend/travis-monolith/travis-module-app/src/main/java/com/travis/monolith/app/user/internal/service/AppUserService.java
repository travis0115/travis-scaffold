package com.travis.monolith.app.user.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.app.user.api.request.AppUserPageReq;
import com.travis.monolith.app.user.api.response.AppUserDashboardResp;
import com.travis.monolith.app.user.api.response.AppUserOptionResp;
import com.travis.monolith.app.user.internal.entity.AppUser;
import java.util.Collection;
import java.util.List;

/** 客户端用户管理服务。 */
public interface AppUserService extends IService<AppUser> {
    /** 分页查询可用的客户端用户选项。 */
    PageResp<AppUserOptionResp> page(AppUserPageReq req);

    /** 根据用户 ID 集合查询可用的客户端用户选项。 */
    List<AppUserOptionResp> listOptionsByIds(Collection<Long> ids);

    /** 获取首页客户端用户概览。 */
    AppUserDashboardResp dashboard();
}
