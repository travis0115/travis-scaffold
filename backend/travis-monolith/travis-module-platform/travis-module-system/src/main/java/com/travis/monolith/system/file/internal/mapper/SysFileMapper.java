package com.travis.monolith.system.file.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.BaseMapperX;
import com.travis.monolith.system.file.internal.entity.SysFile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysFileMapper extends BaseMapperX<SysFile> {}
