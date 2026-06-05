package com.travis.monolith.system.internal.converter;

import com.travis.monolith.system.internal.model.entity.SysUpdateLog;
import com.travis.monolith.system.internal.model.req.SysUpdateLogReq;
import com.travis.monolith.system.internal.model.resp.SysUpdateLogResp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 更新日志对象转换器
 * 处理 SysUpdateLog ↔ SysUpdateLogReq/SysUpdateLogResp 之间的对象映射
 *
 * @author travis
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SysUpdateLogConverter {

    /**
     * SysUpdateLog → SysUpdateLogResp（全部同名字段映射）
     */
    SysUpdateLogResp toResp(SysUpdateLog updateLog);

    List<SysUpdateLogResp> toRespList(List<SysUpdateLog> updateLogs);

    /**
     * SysUpdateLogReq → SysUpdateLog（新增时使用）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    SysUpdateLog toEntity(SysUpdateLogReq req);

    /**
     * SysUpdateLogReq → 更新已有的SysUpdateLog
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    void update(SysUpdateLogReq req, @MappingTarget SysUpdateLog updateLog);
}
