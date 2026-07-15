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

    /** 将创建参数转换为字典项实体。 */
    SysDictItem toEntity(SysDictItemCreateReq req);

    /** 将更新参数写入已有字典项实体。 */
    void update(SysDictItemUpdateReq req, @MappingTarget SysDictItem item);

    /** SysDictItem → SysDictItemResp（全部同名字段映射） */
    SysDictItemResp toResp(SysDictItem item);

    /** 批量将字典项实体转换为响应。 */
    List<SysDictItemResp> toRespList(List<SysDictItem> items);
}
