package com.travis.monolith.system.menu.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.system.menu.internal.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 菜单管理 Mapper 接口，继承 BaseMapperX 提供基础 CRUD 能力
 *
 * @author travis
 */
@Mapper
public interface SysMenuMapper extends BaseMapperX<SysMenu> {

    @Select("SELECT * FROM sys_menu WHERE id = #{id} AND is_deleted = 0 FOR UPDATE")
    SysMenu selectByIdForUpdate(@Param("id") Long id);
}
