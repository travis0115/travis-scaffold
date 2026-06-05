package com.travis.monolith.system.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.internal.model.entity.SysDept;
import com.travis.monolith.system.internal.model.response.dept.SysDeptResp;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 部门对象转换器
 *
 * @author travis
 */
@Mapper(config = BaseMapperConfig.class)
public interface SysDeptConverter {

    /**
     * SysDept → SysDeptResp（基础字段映射）
     * children 需在Service层手动设置
     */
    SysDeptResp toResp(SysDept dept);

    List<SysDeptResp> toRespList(List<SysDept> depts);
}
