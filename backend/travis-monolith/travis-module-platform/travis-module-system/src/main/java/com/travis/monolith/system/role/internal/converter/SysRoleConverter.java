package com.travis.monolith.system.role.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.role.api.request.SysRoleCreateReq;
import com.travis.monolith.system.role.api.request.SysRoleUpdateReq;
import com.travis.monolith.system.role.api.response.SysRoleResp;
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

    /** SysRole → SysRoleResp（基础字段映射） menuIds 需在Service层手动设置 */
    SysRoleResp toResp(SysRole role);

    /** 批量将角色实体转换为响应。 */
    List<SysRoleResp> toRespList(List<SysRole> roles);

    /** 将创建参数转换为角色实体。 */
    SysRole toEntity(SysRoleCreateReq req);

    /** 将更新参数写入已有角色实体。 */
    void update(SysRoleUpdateReq req, @MappingTarget SysRole role);
}
