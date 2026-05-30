package com.travis.monolith.system.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.mapper.BaseMapperX;
import com.travis.monolith.system.internal.model.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色-菜单关联 Mapper 接口，用于 RBAC 权限模型中角色与菜单的关联查询
 *
 * @author travis
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapperX<SysRoleMenu> {
}
