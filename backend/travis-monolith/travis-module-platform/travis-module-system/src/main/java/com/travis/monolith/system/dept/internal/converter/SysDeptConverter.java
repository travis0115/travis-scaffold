package com.travis.monolith.system.dept.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.dept.internal.model.entity.SysDept;
import com.travis.monolith.system.dept.api.model.SysDeptResp;
import java.util.List;
import org.mapstruct.Mapper;

/**
 * 部门对象转换器
 *
 * @author travis
 */
@Mapper(config = BaseMapperConfig.class)
public interface SysDeptConverter {

    /** SysDept → SysDeptResp（基础字段映射） children 需在Service层手动设置 */
    SysDeptResp toResp(SysDept dept);

    List<SysDeptResp> toRespList(List<SysDept> depts);
}
