package com.travis.monolith.app.user.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.app.user.api.response.AppUserInfoResp;
import com.travis.monolith.app.user.internal.entity.AppUser;
import org.mapstruct.Mapper;

/** 客户端用户对象转换器。 */
@Mapper(config = BaseMapperConfig.class)
public interface AppUserConverter {
    /** 将用户实体转换为当前用户信息。 */
    AppUserInfoResp toInfoResp(AppUser user);
}
