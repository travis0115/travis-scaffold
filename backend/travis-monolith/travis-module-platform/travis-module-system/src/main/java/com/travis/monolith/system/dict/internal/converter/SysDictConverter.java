package com.travis.monolith.system.dict.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.dict.api.request.SysDictCreateReq;
import com.travis.monolith.system.dict.api.request.SysDictUpdateReq;
import com.travis.monolith.system.dict.api.response.SysDictResp;
import com.travis.monolith.system.dict.internal.entity.SysDict;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = BaseMapperConfig.class)
public interface SysDictConverter {

    SysDictResp toResp(SysDict dict);

    List<SysDictResp> toRespList(List<SysDict> dicts);

    SysDict toEntity(SysDictCreateReq req);

    void update(SysDictUpdateReq req, @MappingTarget SysDict dict);
}
