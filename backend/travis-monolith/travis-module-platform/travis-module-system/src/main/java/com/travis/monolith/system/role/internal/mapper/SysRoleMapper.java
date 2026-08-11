package com.travis.monolith.system.role.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.system.role.internal.entity.SysRole;
import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 角色管理 Mapper 接口，继承 BaseMapperX 提供基础 CRUD 能力
 *
 * @author travis
 */
@Mapper
public interface SysRoleMapper extends BaseMapperX<SysRole> {

    @Select("SELECT * FROM sys_role WHERE id = #{id} AND is_deleted = 0 FOR UPDATE")
    SysRole selectByIdForUpdate(@Param("id") Long id);

    @Select({
        "<script>",
        "SELECT * FROM sys_role WHERE is_deleted = 0 AND id IN",
        "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
        "#{id}",
        "</foreach>",
        "FOR UPDATE",
        "</script>"
    })
    List<SysRole> selectByIdsForUpdate(@Param("ids") Collection<Long> ids);
}
