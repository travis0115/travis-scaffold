package com.travis.monolith.system.internal.mapper;

import com.travis.infrastructure.framework.mybatis.core.mapper.BaseMapperX;
import com.travis.monolith.system.internal.model.entity.SysUpdateLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统更新日志 Mapper
 *
 * @author travis
 */
@Mapper
public interface SysUpdateLogMapper extends BaseMapperX<SysUpdateLog> {
}
