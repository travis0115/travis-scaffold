package com.travis.monolith.system.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.internal.model.entity.SysUser;
import com.travis.monolith.system.internal.model.request.user.SysUserReq;
import com.travis.monolith.system.internal.model.response.user.SysUserResp;
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

    /** SysUser → SysUserResp（基础字段映射） deptName、roleIds、roleNames、lastLoginLocation 需在Service层手动设置 */
    SysUserResp toResp(SysUser user);

    List<SysUserResp> toRespList(List<SysUser> users);

    SysUser toEntity(SysUserReq req);

    void update(SysUserReq req, @MappingTarget SysUser user);
}
