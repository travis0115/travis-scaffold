package com.travis.monolith.system.user.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.system.user.internal.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 管理员用户 Mapper 接口，继承 BaseMapperX 提供基础 CRUD 能力
 *
 * @author travis
 */
@Mapper
public interface SysUserMapper extends BaseMapperX<SysUser> {

    @Select("SELECT * FROM sys_user WHERE id = #{id} AND is_deleted = 0 FOR UPDATE")
    SysUser selectByIdForUpdate(@Param("id") Long id);
}
