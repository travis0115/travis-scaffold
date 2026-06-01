package com.travis.monolith.system.internal.converter;

import com.travis.monolith.system.internal.model.entity.SysDictItem;
import com.travis.monolith.system.internal.model.resp.SysDictItemResp;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 字典数据项对象转换器
 * 处理 SysDictItem → SysDictItemResp 之间的对象映射
 *
 * @author travis
 */
@Mapper(componentModel = "spring")
public interface SysDictItemConverter {

    /**
     * SysDictItem → SysDictItemResp（全部同名字段映射）
     */
    SysDictItemResp toDictItemResp(SysDictItem item);

    List<SysDictItemResp> toDictItemRespList(List<SysDictItem> items);
}
