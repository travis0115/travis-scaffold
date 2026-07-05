package com.travis.monolith.app.user.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.app.user.api.response.AppUserInfoResp;
import com.travis.monolith.app.user.internal.entity.AppUser;
import org.mapstruct.Mapper;

@Mapper(config = BaseMapperConfig.class)
public interface AppUserConverter {
    AppUserInfoResp toInfoResp(AppUser user);
}
