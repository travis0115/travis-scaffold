package com.travis.monolith.system.menu.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.menu.api.request.SysMenuCreateReq;
import com.travis.monolith.system.menu.api.request.SysMenuUpdateReq;
import com.travis.monolith.system.menu.api.response.SysMenuDetailResp;
import com.travis.monolith.system.menu.api.response.SysMenuListResp;
import com.travis.monolith.system.menu.internal.entity.SysMenu;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/**
 * 菜单对象转换器
 *
 * @author travis
 */
@Mapper(config = BaseMapperConfig.class)
public interface SysMenuConverter {

    /** SysMenu → SysMenuListResp（基础字段映射） children 需在Service层手动设置 */
    SysMenuListResp toResp(SysMenu menu);

    SysMenuDetailResp toDetailResp(SysMenu menu);

    List<SysMenuListResp> toRespList(List<SysMenu> menus);

    SysMenu toEntity(SysMenuCreateReq req);

    void update(SysMenuUpdateReq req, @MappingTarget SysMenu menu);
}
