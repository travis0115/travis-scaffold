package com.travis.monolith.app.user.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.app.user.internal.entity.AppUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AppUserMapper extends BaseMapperX<AppUser> {}
