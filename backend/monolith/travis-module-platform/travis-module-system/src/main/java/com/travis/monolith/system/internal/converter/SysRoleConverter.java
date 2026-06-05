package com.travis.monolith.system.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.internal.model.entity.SysRole;
import com.travis.monolith.system.internal.model.request.role.SysRoleReq;
import com.travis.monolith.system.internal.model.response.role.SysRoleResp;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 角色对象转换器
 *
 * @author travis
 */
@Mapper(config = BaseMapperConfig.class)
public interface SysRoleConverter {

    /**
     * SysRole → SysRoleResp（基础字段映射）
     * menuIds 需在Service层手动设置
     */
    SysRoleResp toResp(SysRole role);

    List<SysRoleResp> toRespList(List<SysRole> roles);

    SysRole toEntity(SysRoleReq req);

    void update(SysRoleReq req, @MappingTarget SysRole role);
}
