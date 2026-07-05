package com.travis.monolith.app.user.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.monolith.app.user.api.response.AppUserOptionResp;
import com.travis.monolith.app.user.internal.entity.AppUser;
import java.util.Collection;
import java.util.List;

public interface AppUserService extends IService<AppUser> {
    List<AppUserOptionResp> listOptions(String keyword, int limit);

    List<AppUserOptionResp> listOptionsByIds(Collection<Long> userIds);
}
