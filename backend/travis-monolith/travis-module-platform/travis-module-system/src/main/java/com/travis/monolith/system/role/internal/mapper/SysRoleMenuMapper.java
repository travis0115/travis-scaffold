package com.travis.monolith.system.role.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.system.role.internal.entity.SysRoleMenu;
import java.util.Collection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 角色-菜单关联 Mapper 接口，用于 RBAC 权限模型中角色与菜单的关联查询
 *
 * @author travis
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapperX<SysRoleMenu> {

    @Select({
        "<script>",
        "SELECT id FROM sys_menu WHERE is_deleted = 0 AND id IN",
        "<foreach collection='menuIds' item='menuId' open='(' separator=',' close=')'>",
        "#{menuId}",
        "</foreach>",
        "FOR UPDATE",
        "</script>"
    })
    java.util.List<Long> selectExistingMenuIdsForUpdate(@Param("menuIds") Collection<Long> menuIds);
}
