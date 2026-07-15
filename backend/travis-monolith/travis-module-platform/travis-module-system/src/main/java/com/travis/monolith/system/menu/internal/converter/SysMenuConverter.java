package com.travis.monolith.system.menu.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.menu.api.request.SysMenuCreateReq;
import com.travis.monolith.system.menu.api.request.SysMenuUpdateReq;
import com.travis.monolith.system.menu.api.response.SysMenuResp;
import com.travis.monolith.system.menu.internal.entity.SysMenu;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * 菜单对象转换器
 *
 * @author travis
 */
@Mapper(config = BaseMapperConfig.class)
public interface SysMenuConverter {

    /** 将菜单实体转换为包含空子节点列表的响应。 */
    @Mapping(target = "children", expression = "java(new java.util.ArrayList<>())")
    SysMenuResp toResp(SysMenu menu);

    /** 批量将菜单实体转换为响应。 */
    List<SysMenuResp> toRespList(List<SysMenu> menus);

    /** 将创建参数转换为菜单实体。 */
    SysMenu toEntity(SysMenuCreateReq req);

    /** 将更新参数写入已有菜单实体。 */
    void update(SysMenuUpdateReq req, @MappingTarget SysMenu menu);
}
