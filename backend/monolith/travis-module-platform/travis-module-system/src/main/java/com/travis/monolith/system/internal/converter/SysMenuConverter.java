package com.travis.monolith.system.internal.converter;

import com.travis.monolith.system.internal.model.entity.SysMenu;
import com.travis.monolith.system.internal.model.req.SysMenuReq;
import com.travis.monolith.system.internal.model.resp.SysMenuResp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 菜单对象转换器
 * 处理 SysMenu ↔ SysMenuReq/SysMenuResp 之间的对象映射
 *
 * @author travis
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SysMenuConverter {

    /**
     * SysMenu → SysMenuResp（基础字段映射）
     * children 需在Service层手动设置
     */
    @Mapping(target = "children", ignore = true)
    SysMenuResp toResp(SysMenu menu);

    List<SysMenuResp> toRespList(List<SysMenu> menus);

    /**
     * SysMenuReq → SysMenu（新增时使用）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    SysMenu toEntity(SysMenuReq req);

    /**
     * SysMenuReq → 更新已有的SysMenu
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    void update(SysMenuReq req, @MappingTarget SysMenu menu);
}
