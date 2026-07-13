package com.travis.monolith.app.user.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.app.user.api.request.AppUserPageReq;
import com.travis.monolith.app.user.api.response.AppUserOptionResp;
import com.travis.monolith.app.user.internal.entity.AppUser;
import java.util.Collection;
import java.util.List;

public interface AppUserService extends IService<AppUser> {
    PageResp<AppUserOptionResp> page(AppUserPageReq req);

    List<AppUserOptionResp> listOptionsByIds(Collection<Long> ids);
}
