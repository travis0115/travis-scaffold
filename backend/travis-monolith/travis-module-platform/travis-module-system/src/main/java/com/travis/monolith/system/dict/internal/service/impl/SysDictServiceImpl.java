package com.travis.monolith.system.dict.internal.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import com.travis.monolith.system.dict.api.request.SysDictCreateReq;
import com.travis.monolith.system.dict.api.request.SysDictItemCreateReq;
import com.travis.monolith.system.dict.api.request.SysDictItemUpdateReq;
import com.travis.monolith.system.dict.api.request.SysDictUpdateReq;
import com.travis.monolith.system.dict.api.response.SysDictItemResp;
import com.travis.monolith.system.dict.internal.converter.SysDictConverter;
import com.travis.monolith.system.dict.internal.converter.SysDictItemConverter;
import com.travis.monolith.system.dict.internal.entity.SysDict;
import com.travis.monolith.system.dict.internal.entity.SysDictItem;
import com.travis.monolith.system.dict.internal.mapper.SysDictMapper;
import com.travis.monolith.system.dict.internal.service.SysDictItemService;
import com.travis.monolith.system.dict.internal.service.SysDictService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 字典管理服务实现，同时代理字典数据项的操作，将字典项的增删改委托给 {@link SysDictItemService}
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "system:dict")
public class SysDictServiceImpl extends ServiceImplX<SysDictMapper, SysDict>
        implements SysDictService {

    /** 字典数据项服务 */
    private final SysDictItemService dictItemService;

    /** 对象转换器 */
    private final SysDictItemConverter itemConverter;

    /** 字典类型转换器 */
    private final SysDictConverter converter;

    /** 缓存管理器 */
    private final CacheManager cacheManager;

    /** 获取字典树形数据（每个字典包含其下的数据项作为 children） */
    @Override
    @Cacheable(key = "'tree:all'")
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
                                                itemConverter::toResp, Collectors.toList())));
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
        Page<SysDict> page = page(pageNum, pageSize, wrapper);
        return PageConverter.toResp(page);
    }

    /** 获取字典类型详情 */
    @Override
    @Cacheable(key = "'detail:'+#id")
    public SysDict getById(Long id) {
        return getByIdOrThrow(id);
    }

    /** 新增字典类型 */
    @Override
    @Transactional
    @CacheEvict(key = "'tree:all'")
    public void create(SysDictCreateReq req) {
        // 检查字典类型编码唯一性
        long count =
                count(
                        new LambdaQueryWrapperX<SysDict>()
                                .eq(SysDict::getDictType, req.getDictType()));
        if (count > 0) {
            throw new BizException(SystemErrorCode.DICT_TYPE_EXISTS);
        }
        save(converter.toEntity(req));
    }

    /** 更新字典类型 */
    @Override
    @Transactional
    @Caching(evict = {@CacheEvict(key = "'tree:all'"), @CacheEvict(key = "'detail:'+#id")})
    public void update(Long id, SysDictUpdateReq req) {
        SysDict dict = getByIdOrThrow(id);
        // 检查字典类型编码唯一性（排除自身）
        long count =
                count(
                        new LambdaQueryWrapperX<SysDict>()
                                .eq(SysDict::getDictType, req.getDictType())
                                .ne(SysDict::getId, id));
        if (count > 0) {
            throw new BizException(SystemErrorCode.DICT_TYPE_EXISTS);
        }
        converter.update(req, dict);
        updateById(dict);
    }

    /** 修改字典类型状态 */
    @Override
    @Transactional
    @Caching(evict = {@CacheEvict(key = "'tree:all'"), @CacheEvict(key = "'detail:'+#id")})
    public void updateStatus(Long id, Integer status) {
        SysDict dict = getByIdOrThrow(id);
        dict.setStatus(status);
        updateById(dict);
    }

    /** 删除字典类型（同时删除其下所有字典数据项） */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(key = "'tree:all'"),
                @CacheEvict(key = "'detail:'+#id"),
                @CacheEvict(key = "'item-list:' + #id")
            })
    public void deleteById(Long id) {
        // 删除字典下的所有数据项
        dictItemService.remove(
                new LambdaQueryWrapperX<SysDictItem>().eq(SysDictItem::getDictId, id));
        removeById(id);
    }

    /** 查询指定字典类型下的所有数据项，按排序号升序 */
    @Override
    @Cacheable(key = "'item-list:' + #dictId")
    public List<SysDictItemResp> listItems(Long dictId) {
        List<SysDictItem> items =
                dictItemService.list(
                        new LambdaQueryWrapperX<SysDictItem>()
                                .eq(SysDictItem::getDictId, dictId)
                                .orderByAsc(SysDictItem::getSort));
        return itemConverter.toRespList(items);
    }

    /** 新增字典数据项（委托给 {@link SysDictItemService}） */
    @Override
    @Caching(
            evict = {
                @CacheEvict(key = "'tree:all'"),
                @CacheEvict(key = "'item-list:' + #req.dictId")
            })
    public void createItem(SysDictItemCreateReq req) {
        dictItemService.create(req);
    }

    /** 更新字典数据项（委托给 {@link SysDictItemService}） */
    @Override
    @Caching(
            evict = {
                @CacheEvict(key = "'tree:all'"),
                @CacheEvict(key = "'item-list:' + #req.dictId")
            })
    public void updateItem(Long id, SysDictItemUpdateReq req) {
        dictItemService.update(id, req);
    }

    /** 修改字典数据项状态 */
    @Override
    public void updateItemStatus(Long id, Integer status) {
        SysDictItem item = dictItemService.getById(id);
        dictItemService.updateStatus(id, status);
        if (item != null) {
            var cache = cacheManager.getCache("system:dict");
            if (cache != null) {
                cache.evict("tree:all");
                cache.evict("item-list:" + item.getDictId());
            }
        }
    }

    /** 删除字典数据项（委托给 {@link SysDictItemService}） */
    @Override
    public void deleteItemById(Long id) {
        SysDictItem item = dictItemService.getById(id);
        dictItemService.deleteById(id);
        if (item != null) {
            var cache = cacheManager.getCache("system:dict");
            if (cache != null) {
                cache.evict("tree:all");
                cache.evict("item-list:" + item.getDictId());
            }
        }
    }
}
