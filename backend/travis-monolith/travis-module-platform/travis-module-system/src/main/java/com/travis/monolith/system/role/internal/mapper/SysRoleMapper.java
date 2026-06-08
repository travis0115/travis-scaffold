package com.travis.monolith.system.role.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.system.role.internal.model.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色管理 Mapper 接口，继承 BaseMapperX 提供基础 CRUD 能力
 *
 * @author travis
 */
@Mapper
public interface SysRoleMapper extends BaseMapperX<SysRole> {}
