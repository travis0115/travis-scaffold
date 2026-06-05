package com.travis.monolith.system.internal.converter;

import com.travis.monolith.system.internal.model.entity.SysDictItem;
import com.travis.monolith.system.internal.model.resp.SysDictItemResp;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * 字典数据项对象转换器
 * 处理 SysDictItem → SysDictItemResp 之间的对象映射
 *
 * @author travis
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SysDictItemConverter {

    /**
     * SysDictItem → SysDictItemResp（全部同名字段映射）
     */
    SysDictItemResp toResp(SysDictItem item);

    List<SysDictItemResp> toRespList(List<SysDictItem> items);
}
