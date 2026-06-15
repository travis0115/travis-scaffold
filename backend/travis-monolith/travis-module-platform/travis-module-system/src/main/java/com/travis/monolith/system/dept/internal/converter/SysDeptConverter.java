package com.travis.monolith.system.dept.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.dept.api.request.SysDeptCreateReq;
import com.travis.monolith.system.dept.api.request.SysDeptUpdateReq;
import com.travis.monolith.system.dept.api.response.SysDeptResp;
import com.travis.monolith.system.dept.internal.entity.SysDept;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 部门对象转换器
 *
 * @author travis
 */
@Mapper(config = BaseMapperConfig.class)
public interface SysDeptConverter {

    @Mapping(target = "parentId", defaultValue = "0L")
    SysDept toEntity(SysDeptCreateReq req);

    @Mapping(target = "parentId", defaultValue = "0L")
    void update(SysDeptUpdateReq req, @MappingTarget SysDept dept);

    @Mapping(target = "children", expression = "java(new java.util.ArrayList<>())")
    SysDeptResp toResp(SysDept dept);

    List<SysDeptResp> toRespList(List<SysDept> depts);
}
