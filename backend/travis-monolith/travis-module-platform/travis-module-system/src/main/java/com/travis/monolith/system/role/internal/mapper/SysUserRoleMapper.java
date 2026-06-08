package com.travis.monolith.system.role.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.system.role.internal.model.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户-角色关联 Mapper 接口，用于用户与角色的关联查询
 *
 * @author travis
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapperX<SysUserRole> {}
