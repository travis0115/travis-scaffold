package com.travis.monolith.system.internal.converter;

import com.travis.monolith.system.internal.model.entity.SysDept;
import com.travis.monolith.system.internal.model.resp.SysDeptResp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * 部门对象转换器
 * 处理 SysDept → SysDeptResp 之间的对象映射
 *
 * @author travis
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SysDeptConverter {

    /**
     * SysDept → SysDeptResp（基础字段映射）
     * children 需在Service层手动设置
     */
    @Mapping(target = "children", ignore = true)
    SysDeptResp toResp(SysDept dept);

    List<SysDeptResp> toRespList(List<SysDept> depts);
}
