package com.travis.monolith.system.user.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.user.api.request.SysUserCreateReq;
import com.travis.monolith.system.user.api.request.SysUserUpdateReq;
import com.travis.monolith.system.user.api.request.UpdateAvatarReq;
import com.travis.monolith.system.user.api.request.UserProfileReq;
import com.travis.monolith.system.user.api.response.SysUserDetailResp;
import com.travis.monolith.system.user.api.response.SysUserPageResp;
import com.travis.monolith.system.user.internal.entity.SysUser;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/**
 * 用户对象转换器
 *
 * @author travis
 */
@Mapper(config = BaseMapperConfig.class)
public interface SysUserConverter {

    /** SysUser → SysUserPageResp（基础字段映射） deptName、roleIds、roleNames、lastLoginLocation 需在Service层手动设置 */
    SysUserPageResp toResp(SysUser user);

    SysUserDetailResp toDetailResp(SysUser user);

    List<SysUserPageResp> toRespList(List<SysUser> users);

    SysUser toEntity(SysUserCreateReq req);

    void update(SysUserUpdateReq req, @MappingTarget SysUser user);

    void updateProfile(UserProfileReq req, @MappingTarget SysUser user);

    void updateAvatar(UpdateAvatarReq req, @MappingTarget SysUser user);
}
