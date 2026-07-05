package com.travis.monolith.system.log.loginlog.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.log.loginlog.api.response.SysLoginLogResp;
import com.travis.monolith.system.log.loginlog.internal.entity.SysLoginLog;
import java.util.List;
import org.mapstruct.Mapper;

/**
 * 登录日志转换器
 *
 * @author travis
 */
@Mapper(config = BaseMapperConfig.class)
public interface SysLoginLogConverter {

    SysLoginLogResp toResp(SysLoginLog notice);

    List<SysLoginLogResp> toRespList(List<SysLoginLog> notices);
}
