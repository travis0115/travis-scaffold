package com.travis.monolith.system.menu.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.menu.api.request.SysMenuCreateReq;
import com.travis.monolith.system.menu.api.request.SysMenuUpdateReq;
import com.travis.monolith.system.menu.api.response.SysMenuResp;
import com.travis.monolith.system.menu.internal.entity.SysMenu;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 菜单对象转换器
 *
 * @author travis
 */
@Mapper(config = BaseMapperConfig.class)
public interface SysMenuConverter {

    @Mapping(target = "children", expression = "java(new java.util.ArrayList<>())")
    SysMenuResp toResp(SysMenu menu);

    List<SysMenuResp> toRespList(List<SysMenu> menus);

    SysMenu toEntity(SysMenuCreateReq req);

    void update(SysMenuUpdateReq req, @MappingTarget SysMenu menu);
}
