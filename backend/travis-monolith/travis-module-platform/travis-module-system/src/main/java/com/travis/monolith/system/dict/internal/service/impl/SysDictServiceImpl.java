package com.travis.monolith.system.dict.internal.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.web.core.exception.BizException;
import com.travis.monolith.system.common.api.SystemErrorCode;
import com.travis.monolith.system.dict.api.response.SysDictItemResp;
import com.travis.monolith.system.dict.internal.converter.SysDictItemConverter;
import com.travis.monolith.system.dict.internal.entity.SysDict;
import com.travis.monolith.system.dict.internal.entity.SysDictItem;
import com.travis.monolith.system.dict.internal.mapper.SysDictMapper;
import com.travis.monolith.system.dict.internal.request.SysDictItemReq;
import com.travis.monolith.system.dict.internal.request.SysDictReq;
import com.travis.monolith.system.dict.internal.service.SysDictItemService;
import com.travis.monolith.system.dict.internal.service.SysDictService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 字典管理服务实现，同时代理字典数据项的操作，将字典项的增删改委托给 {@link SysDictItemService}
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict>
        implements SysDictService {

    /** 字典数据项服务 */
    private final SysDictItemService dictItemService;

    /** 对象转换器 */
    private final SysDictItemConverter converter;

    /** 获取字典树形数据（每个字典包含其下的数据项作为 children） */
    @Override
    public List<SysDict> listTree() {
        // 查询所有字典类型
        List<SysDict> dictList = list();
        if (dictList.isEmpty()) {
            return dictList;
        }
        // 批量查询所有字典类型下的数据项
        List<Long> dictIds = dictList.stream().map(SysDict::getId).toList();
        List<SysDictItem> allItems =
                dictItemService.list(
                        new LambdaQueryWrapperX<SysDictItem>()
                                .in(SysDictItem::getDictId, dictIds)
                                .orderByAsc(SysDictItem::getSort));
        // 按 dictId 分组
        Map<Long, List<SysDictItemResp>> itemsGroup =
                allItems.stream()
                        .collect(
                                Collectors.groupingBy(
                                        SysDictItem::getDictId,
                                        Collectors.mapping(
                                                converter::toResp, Collectors.toList())));
        // 为每个字典设置 children
        dictList.forEach(
                dict -> dict.setChildren(itemsGroup.getOrDefault(dict.getId(), List.of())));
        return dictList;
    }

    /** 分页查询字典类型列表，支持按名称、类型编码、状态筛选 */
    @Override
    public PageResp<SysDict> page(
            String dictName, String dictType, Integer status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapperX<SysDict> wrapper =
                new LambdaQueryWrapperX<SysDict>()
                        .likeIfPresent(SysDict::getDictName, dictName)
                        .likeIfPresent(SysDict::getDictType, dictType)
                        .eqIfPresent(SysDict::getStatus, status)
                        .orderByDesc(SysDict::getCreateTime);
        Page<SysDict> page = page(new Page<>(pageNum, pageSize), wrapper);
        return PageConverter.toResp(page);
    }

    /** 获取字典类型详情 */
    @Override
    public SysDict getById(Long id) {
        SysDict dict = super.getById(id);
        if (dict == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        return dict;
    }

    /** 新增字典类型 */
    @Override
    @Transactional
    public void create(SysDictReq req) {
        // 检查字典类型编码唯一性
        long count =
                count(
                        new LambdaQueryWrapperX<SysDict>()
                                .eq(SysDict::getDictType, req.getDictType()));
        if (count > 0) {
            throw new BizException(SystemErrorCode.SYSTEM_DICT_TYPE_EXISTS);
        }
        SysDict dict = new SysDict();
        dict.setDictName(req.getDictName());
        dict.setDictType(req.getDictType());
        dict.setStatus(req.getStatus());
        dict.setRemark(req.getRemark());
        save(dict);
    }

    /** 更新字典类型 */
    @Override
    @Transactional
    public void update(Long id, SysDictReq req) {
        SysDict dict = super.getById(id);
        if (dict == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        // 检查字典类型编码唯一性（排除自身）
        long count =
                count(
                        new LambdaQueryWrapperX<SysDict>()
                                .eq(SysDict::getDictType, req.getDictType())
                                .ne(SysDict::getId, id));
        if (count > 0) {
            throw new BizException(SystemErrorCode.SYSTEM_DICT_TYPE_EXISTS);
        }
        dict.setDictName(req.getDictName());
        dict.setDictType(req.getDictType());
        dict.setStatus(req.getStatus());
        dict.setRemark(req.getRemark());
        updateById(dict);
    }

    /** 删除字典类型（同时删除其下所有字典数据项） */
    @Override
    @Transactional
    @CacheEvict(value = "system:dict:items", key = "#id")
    public void deleteById(Long id) {
        // 删除字典下的所有数据项
        dictItemService.remove(
                new LambdaQueryWrapperX<SysDictItem>().eq(SysDictItem::getDictId, id));
        removeById(id);
    }

    /** 查询指定字典类型下的所有数据项，按排序号升序 */
    @Override
    public List<SysDictItemResp> listItems(Long dictId) {
        List<SysDictItem> items =
                dictItemService.list(
                        new LambdaQueryWrapperX<SysDictItem>()
                                .eq(SysDictItem::getDictId, dictId)
                                .orderByAsc(SysDictItem::getSort));
        return converter.toRespList(items);
    }

    /** 新增字典数据项（委托给 {@link SysDictItemService}） */
    @Override
    @CacheEvict(value = "system:dict:items", key = "#req.dictId")
    public void createItem(SysDictItemReq req) {
        dictItemService.create(req);
    }

    /** 更新字典数据项（委托给 {@link SysDictItemService}） */
    @Override
    @CacheEvict(value = "system:dict:items", key = "#req.dictId")
    public void updateItem(Long id, SysDictItemReq req) {
        dictItemService.update(id, req);
    }

    /** 删除字典数据项（委托给 {@link SysDictItemService}） */
    @Override
    @CacheEvict(value = "system:dict:items", allEntries = true)
    public void deleteItemById(Long id) {
        dictItemService.deleteById(id);
    }
}
