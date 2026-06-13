package com.travis.monolith.system.role.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.role.api.request.SysRoleCreateReq;
import com.travis.monolith.system.role.api.request.SysRoleUpdateReq;
import com.travis.monolith.system.role.api.response.SysRoleDetailResp;
import com.travis.monolith.system.role.api.response.SysRoleListResp;
import com.travis.monolith.system.role.api.response.SysRolePageResp;
import com.travis.monolith.system.role.internal.entity.SysRole;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/**
 * 角色对象转换器
 *
 * @author travis
 */
@Mapper(config = BaseMapperConfig.class)
public interface SysRoleConverter {

    /** SysRole → SysRolePageResp（基础字段映射） menuIds 需在Service层手动设置 */
    SysRolePageResp toResp(SysRole role);

    SysRoleDetailResp toDetailResp(SysRole role);

    List<SysRoleListResp> toListResp(List<SysRole> roles);

    List<SysRolePageResp> toRespList(List<SysRole> roles);

    SysRole toEntity(SysRoleCreateReq req);

    void update(SysRoleUpdateReq req, @MappingTarget SysRole role);
}
