package com.travis.monolith.system.file.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.system.file.internal.entity.SysFileStorageConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysFileStorageConfigMapper extends BaseMapperX<SysFileStorageConfig> {
    @Select(
            """
            SELECT EXISTS(
                SELECT 1
                FROM sys_file
                WHERE storage_config_id = #{storageConfigId}
                  AND is_deleted = 0
            )
            """)
    boolean existsFile(@Param("storageConfigId") Long storageConfigId);
}
