package com.travis.monolith.system.dict.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.dict.api.request.SysDictCreateReq;
import com.travis.monolith.system.dict.api.request.SysDictUpdateReq;
import com.travis.monolith.system.dict.api.response.SysDictResp;
import com.travis.monolith.system.dict.internal.entity.SysDict;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/** 字典类型对象转换器。 */
@Mapper(config = BaseMapperConfig.class)
public interface SysDictConverter {

    /** 将字典实体转换为响应。 */
    SysDictResp toResp(SysDict dict);

    /** 批量将字典实体转换为响应。 */
    List<SysDictResp> toRespList(List<SysDict> dicts);

    /** 将创建参数转换为字典实体。 */
    SysDict toEntity(SysDictCreateReq req);

    /** 将更新参数写入已有字典实体。 */
    void update(SysDictUpdateReq req, @MappingTarget SysDict dict);
}
