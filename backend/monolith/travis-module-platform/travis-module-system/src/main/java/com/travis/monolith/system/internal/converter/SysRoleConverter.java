package com.travis.monolith.system.internal.converter;

import com.travis.monolith.system.internal.model.entity.SysRole;
import com.travis.monolith.system.internal.model.req.SysRoleReq;
import com.travis.monolith.system.internal.model.resp.SysRoleResp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 角色对象转换器
 * 处理 SysRole ↔ SysRoleReq/SysRoleResp 之间的对象映射
 *
 * @author travis
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SysRoleConverter {

    /**
     * SysRole → SysRoleResp（基础字段映射）
     * menuIds 需在Service层手动设置
     */
    @Mapping(target = "menuIds", ignore = true)
    SysRoleResp toResp(SysRole role);

    List<SysRoleResp> toRespList(List<SysRole> roles);

    /**
     * SysRoleReq → SysRole（新增时使用）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    SysRole toEntity(SysRoleReq req);

    /**
     * SysRoleReq → 更新已有的SysRole
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void update(SysRoleReq req, @MappingTarget SysRole role);
}
