package com.travis.monolith.system.config.internal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travis.monolith.system.config.internal.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统配置 Mapper
 *
 * @author travis
 */
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {}
