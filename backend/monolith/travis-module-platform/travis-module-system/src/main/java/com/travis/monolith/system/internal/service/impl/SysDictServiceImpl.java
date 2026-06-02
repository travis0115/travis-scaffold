package com.travis.monolith.system.internal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.framework.web.core.exception.BizException;
import com.travis.infrastructure.framework.web.core.exception.CommonErrorCode;
import com.travis.infrastructure.framework.web.core.model.PageResult;
import com.travis.monolith.system.internal.converter.SysDictItemConverter;
import com.travis.monolith.system.internal.mapper.SysDictMapper;
import com.travis.monolith.system.internal.model.entity.SysDict;
import com.travis.monolith.system.internal.model.entity.SysDictItem;
import com.travis.monolith.system.internal.model.req.SysDictItemReq;
import com.travis.monolith.system.internal.model.req.SysDictReq;
import com.travis.monolith.system.internal.model.resp.SysDictItemResp;
import com.travis.monolith.system.internal.service.SysDictItemService;
import com.travis.monolith.system.internal.service.SysDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字典管理服务实现，同时代理字典数据项的操作，将字典项的增删改委托给 {@link SysDictItemService}
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements SysDictService {

    /** 字典数据项服务 */
    private final SysDictItemService dictItemService;

    /** 对象转换器 */
    private final SysDictItemConverter converter;

    /**
     * 获取字典树形数据（每个字典包含其下的数据项作为 children）
     */
    @Override
    public List<SysDict> getDictTree() {
        // 查询所有字典类型
        List<SysDict> dictList = list();
        if (dictList.isEmpty()) {
            return dictList;
        }
        // 批量查询所有字典类型下的数据项
        List<Long> dictIds = dictList.stream().map(SysDict::getId).toList();
        List<SysDictItem> allItems = dictItemService.list(new LambdaQueryWrapper<SysDictItem>()
                .in(SysDictItem::getDictId, dictIds)
                .orderByAsc(SysDictItem::getSort));
        // 按 dictId 分组
        Map<Long, List<SysDictItemResp>> itemsGroup = allItems.stream()
                .collect(Collectors.groupingBy(
                        SysDictItem::getDictId,
                        Collectors.mapping(converter::toDictItemResp, Collectors.toList())));
        // 为每个字典设置 children
        dictList.forEach(dict ->
                dict.setChildren(itemsGroup.getOrDefault(dict.getId(), List.of())));
        return dictList;
    }

    /**
     * 分页查询字典类型列表，支持按名称、类型编码、状态筛选
     */
    @Override
    public PageResult<SysDict> getDictPage(String dictName, String dictType, Integer status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<SysDict>()
                .like(dictName != null, SysDict::getDictName, dictName)
                .like(dictType != null, SysDict::getDictType, dictType)
                .eq(status != null, SysDict::getStatus, status)
                .orderByDesc(SysDict::getCreateTime);
        Page<SysDict> page = page(new Page<>(pageNum, pageSize), wrapper);
        return toPageResult(page);
    }

    /**
     * 获取字典类型详情
     */
    @Override
    public SysDict getDictDetail(Long id) {
        SysDict dict = getById(id);
        if (dict == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        return dict;
    }

    /**
     * 新增字典类型
     */
    @Override
    @Transactional
    public void addDict(SysDictReq req) {
        SysDict dict = new SysDict();
        dict.setDictName(req.getDictName());
        dict.setDictType(req.getDictType());
        dict.setStatus(req.getStatus());
        dict.setRemark(req.getRemark());
        save(dict);
    }

    /**
     * 更新字典类型
     */
    @Override
    @Transactional
    public void updateDict(Long id, SysDictReq req) {
        SysDict dict = getById(id);
        if (dict == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        dict.setDictName(req.getDictName());
        dict.setDictType(req.getDictType());
        dict.setStatus(req.getStatus());
        dict.setRemark(req.getRemark());
        updateById(dict);
    }

    /**
     * 删除字典类型（同时删除其下所有字典数据项）
     */
    @Override
    @Transactional
    @CacheEvict(value = "system:dict:items", key = "#id")
    public void deleteDict(Long id) {
        // 删除字典下的所有数据项
        dictItemService.remove(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictId, id));
        removeById(id);
    }

    /**
     * 查询指定字典类型下的所有数据项，按排序号升序
     */
    @Override
    public List<SysDictItemResp> getDictItems(Long dictId) {
        List<SysDictItem> items = dictItemService.list(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictId, dictId)
                .orderByAsc(SysDictItem::getSort));
        return converter.toDictItemRespList(items);
    }

    /**
     * 新增字典数据项（委托给 {@link SysDictItemService}）
     */
    @Override
    @CacheEvict(value = "system:dict:items", key = "#req.dictId")
    public void addDictItem(SysDictItemReq req) {
        dictItemService.addDictItem(req);
    }

    /**
     * 更新字典数据项（委托给 {@link SysDictItemService}）
     */
    @Override
    @CacheEvict(value = "system:dict:items", key = "#req.dictId")
    public void updateDictItem(Long id, SysDictItemReq req) {
        dictItemService.updateDictItem(id, req);
    }

    /**
     * 删除字典数据项（委托给 {@link SysDictItemService}）
     */
    @Override
    @CacheEvict(value = "system:dict:items", allEntries = true)
    public void deleteDictItem(Long id) {
        dictItemService.deleteDictItem(id);
    }

    /**
     * MyBatis-Plus 分页对象转统一的分页结果
     *
     * @param page 分页对象
     * @param <T>  数据类型
     * @return 分页结果
     */
    private <T> PageResult<T> toPageResult(Page<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal(),
                (int) page.getCurrent(), (int) page.getSize(), (int) page.getPages());
    }
}
