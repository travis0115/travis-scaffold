package com.travis.monolith.system.version.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.version.api.request.SysVersionCreateReq;
import com.travis.monolith.system.version.api.request.SysVersionUpdateReq;
import com.travis.monolith.system.version.api.response.SysVersionResp;
import com.travis.monolith.system.version.internal.entity.SysVersion;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/**
 * 版本日志对象转换器
 *
 * @author travis
 */
@Mapper(config = BaseMapperConfig.class)
public interface SysVersionConverter {

    /** 将版本实体转换为响应。 */
    SysVersionResp toResp(SysVersion versionLog);

    /** 批量将版本实体转换为响应。 */
    List<SysVersionResp> toRespList(List<SysVersion> versionLogs);

    /** 将创建参数转换为版本实体。 */
    SysVersion toEntity(SysVersionCreateReq req);

    /** 将更新参数写入已有版本实体。 */
    void update(SysVersionUpdateReq req, @MappingTarget SysVersion versionLog);
}
