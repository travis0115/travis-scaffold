package com.travis.monolith.system.user.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.user.api.request.SysUserCreateReq;
import com.travis.monolith.system.user.api.request.SysUserProfileReq;
import com.travis.monolith.system.user.api.request.SysUserUpdateAvatarReq;
import com.travis.monolith.system.user.api.request.SysUserUpdateReq;
import com.travis.monolith.system.user.api.response.SysUserResp;
import com.travis.monolith.system.user.internal.entity.SysUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 用户对象转换器
 *
 * @author travis
 */
@Mapper(config = BaseMapperConfig.class)
public interface SysUserConverter {

    /** SysUser → SysUserResp（基础字段映射） deptName、roleIds、roleNames、lastLoginLocation 需在Service层手动设置 */
    SysUserResp toResp(SysUser user);

    List<SysUserResp> toRespList(List<SysUser> users);

    @Mapping(target = "deptId", defaultValue = "0L")
    SysUser toEntity(SysUserCreateReq req);

    @Mapping(target = "deptId", defaultValue = "0L")
    void update(SysUserUpdateReq req, @MappingTarget SysUser user);

    void update(SysUserProfileReq req, @MappingTarget SysUser user);

    void update(SysUserUpdateAvatarReq req, @MappingTarget SysUser user);
}
