package com.travis.monolith.system.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.internal.model.entity.SysMenu;
import com.travis.monolith.system.internal.model.request.menu.SysMenuReq;
import com.travis.monolith.system.internal.model.response.menu.SysMenuResp;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 菜单对象转换器
 *
 * @author travis
 */
@Mapper(config = BaseMapperConfig.class)
public interface SysMenuConverter {

    /**
     * SysMenu → SysMenuResp（基础字段映射）
     * children 需在Service层手动设置
     */
    SysMenuResp toResp(SysMenu menu);

    List<SysMenuResp> toRespList(List<SysMenu> menus);

    SysMenu toEntity(SysMenuReq req);

    void update(SysMenuReq req, @MappingTarget SysMenu menu);
}
