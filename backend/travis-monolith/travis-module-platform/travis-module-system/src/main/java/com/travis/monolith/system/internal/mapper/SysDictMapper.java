package com.travis.monolith.system.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.mapper.BaseMapperX;
import com.travis.monolith.system.internal.model.entity.SysDict;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典类型管理 Mapper 接口，继承 BaseMapperX 提供基础 CRUD 能力
 *
 * @author travis
 */
@Mapper
public interface SysDictMapper extends BaseMapperX<SysDict> {}
