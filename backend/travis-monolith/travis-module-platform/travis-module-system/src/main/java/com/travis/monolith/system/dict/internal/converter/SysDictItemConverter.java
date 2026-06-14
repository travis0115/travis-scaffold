package com.travis.monolith.system.dict.internal.converter;

import com.travis.infrastructure.common.mapstruct.BaseMapperConfig;
import com.travis.monolith.system.dict.api.request.SysDictItemCreateReq;
import com.travis.monolith.system.dict.api.request.SysDictItemUpdateReq;
import com.travis.monolith.system.dict.api.response.SysDictItemResp;
import com.travis.monolith.system.dict.internal.entity.SysDictItem;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/**
 * 字典数据项对象转换器
 *
 * @author travis
 */
@Mapper(config = BaseMapperConfig.class)
public interface SysDictItemConverter {

    SysDictItem toEntity(SysDictItemCreateReq req);

    void update(SysDictItemUpdateReq req, @MappingTarget SysDictItem item);

    /** SysDictItem → SysDictItemResp（全部同名字段映射） */
    SysDictItemResp toResp(SysDictItem item);

    List<SysDictItemResp> toRespList(List<SysDictItem> items);
}
