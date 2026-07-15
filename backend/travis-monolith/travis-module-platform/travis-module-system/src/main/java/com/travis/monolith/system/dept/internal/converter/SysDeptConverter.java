package com.travis.monolith.system.dept.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.dept.api.request.SysDeptCreateReq;
import com.travis.monolith.system.dept.api.request.SysDeptUpdateReq;
import com.travis.monolith.system.dept.api.response.SysDeptResp;
import com.travis.monolith.system.dept.internal.entity.SysDept;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * 部门对象转换器
 *
 * @author travis
 */
@Mapper(config = BaseMapperConfig.class)
public interface SysDeptConverter {

    /** 将创建参数转换为部门实体。 */
    @Mapping(target = "parentId", defaultValue = "0L")
    SysDept toEntity(SysDeptCreateReq req);

    /** 将更新参数写入已有部门实体。 */
    @Mapping(target = "parentId", defaultValue = "0L")
    void update(SysDeptUpdateReq req, @MappingTarget SysDept dept);

    /** 将部门实体转换为包含空子节点列表的响应。 */
    @Mapping(target = "children", expression = "java(new java.util.ArrayList<>())")
    SysDeptResp toResp(SysDept dept);

    /** 批量将部门实体转换为响应。 */
    List<SysDeptResp> toRespList(List<SysDept> depts);
}
