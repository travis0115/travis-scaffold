package com.travis.monolith.system.file.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.system.file.internal.entity.SysFileFolder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysFileFolderMapper extends BaseMapperX<SysFileFolder> {

    @Select("SELECT * FROM sys_file_folder WHERE id = #{id} AND is_deleted = 0 FOR UPDATE")
    SysFileFolder selectByIdForUpdate(@Param("id") Long id);
}
