package com.travis.monolith.system.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.mapper.BaseMapperX;
import com.travis.monolith.system.internal.model.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户-角色关联 Mapper 接口，用于用户与角色的关联查询
 *
 * @author travis
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapperX<SysUserRole> {}
