package com.travis.monolith.system.dict.internal.service.impl;

import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import com.travis.monolith.system.dict.api.request.SysDictCreateReq;
import com.travis.monolith.system.dict.api.request.SysDictPageReq;
import com.travis.monolith.system.dict.api.request.SysDictUpdateReq;
import com.travis.monolith.system.dict.api.response.SysDictResp;
import com.travis.monolith.system.dict.internal.converter.SysDictConverter;
import com.travis.monolith.system.dict.internal.converter.SysDictItemConverter;
import com.travis.monolith.system.dict.internal.entity.SysDict;
import com.travis.monolith.system.dict.internal.entity.SysDictItem;
import com.travis.monolith.system.dict.internal.mapper.SysDictMapper;
import com.travis.monolith.system.dict.internal.service.SysDictItemService;
import com.travis.monolith.system.dict.internal.service.SysDictService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 字典管理服务实现
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

    /** 获取字典树形数据（每个字典包含其下的数据项作为 children） */
    @Override
    @Cacheable(key = "'tree:enabled'")
    public List<SysDictResp> listTree() {
        // 查询所有字典类型
        var dictList =
                list(
                        new LambdaQueryWrapperX<SysDict>()
                                .eq(SysDict::getStatus, Status.ENABLED.getValue())
                                .orderByAsc(SysDict::getCreateTime));
        var respList = converter.toRespList(dictList);
        if (respList.isEmpty()) {
            return respList;
        }
        // 批量查询所有字典类型下的数据项
        var allItems = dictItemService.listEnabled();
        // 按 dictId 分组
        var itemsGroup =
                allItems.stream()
                        .filter(item -> Status.ENABLED.getValue().equals(item.getStatus()))
                        .collect(
                                Collectors.groupingBy(
                                        SysDictItem::getDictId,
                                        Collectors.mapping(
                                                itemConverter::toResp, Collectors.toList())));
        // 为每个字典设置 children
        respList.forEach(
                dict -> dict.setChildren(itemsGroup.getOrDefault(dict.getId(), List.of())));
        return respList;
    }

    /** 分页查询字典类型列表，支持按名称、字典编码、状态筛选 */
    @Override
    public PageResp<SysDictResp> page(SysDictPageReq req) {
        var wrapper =
                new LambdaQueryWrapperX<SysDict>()
                        .likeIfPresent(SysDict::getDictName, req.getDictName())
                        .likeIfPresent(SysDict::getDictCode, req.getDictCode())
                        .eqIfPresent(SysDict::getStatus, req.getStatus())
                        .orderByAsc(SysDict::getCreateTime);
        var page = page(req.getPageNum(), req.getPageSize(), wrapper);
        return PageConverter.toResp(page.convert(converter::toResp));
    }

    /** 获取字典类型详情 */
    @Override
    @Cacheable(key = "'detail:'+#id")
    public SysDictResp getById(Long id) {
        return converter.toResp(getByIdOrThrow(id));
    }

    /** 新增字典类型 */
    @Override
    @Transactional
    @CacheEvict(key = "'tree:enabled'")
    public void create(SysDictCreateReq req) {
        // 检查字典编码唯一性
        var count =
                count(
                        new LambdaQueryWrapperX<SysDict>()
                                .eq(SysDict::getDictCode, req.getDictCode()));
        if (count > 0) {
            throw new BizException(SystemErrorCode.DICT_CODE_EXISTS);
        }
        save(converter.toEntity(req));
    }

    /** 更新字典类型 */
    @Override
    @Transactional
    @Caching(evict = {@CacheEvict(key = "'tree:enabled'"), @CacheEvict(key = "'detail:'+#id")})
    public void update(Long id, SysDictUpdateReq req) {
        var dict = getByIdOrThrow(id);
        // 检查字典编码唯一性（排除自身）
        var count =
                count(
                        new LambdaQueryWrapperX<SysDict>()
                                .eq(SysDict::getDictCode, req.getDictCode())
                                .ne(SysDict::getId, id));
        if (count > 0) {
            throw new BizException(SystemErrorCode.DICT_CODE_EXISTS);
        }
        converter.update(req, dict);
        updateById(dict);
    }

    /** 修改字典类型状态 */
    @Override
    @Transactional
    @Caching(evict = {@CacheEvict(key = "'tree:enabled'"), @CacheEvict(key = "'detail:'+#id")})
    public void updateStatus(Long id, Integer status) {
        var dict = getByIdOrThrow(id);
        dict.setStatus(status);
        updateById(dict);
    }

    /** 删除字典类型（同时删除其下所有字典数据项） */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(key = "'tree:enabled'"),
                @CacheEvict(key = "'detail:'+#id"),
                @CacheEvict(key = "'item:list:enabled'"),
                @CacheEvict(key = "'item:list:' + #id")
            })
    public void deleteById(Long id) {
        getByIdOrThrow(id);
        // 删除字典下的所有数据项
        dictItemService.remove(
                new LambdaQueryWrapperX<SysDictItem>().eq(SysDictItem::getDictId, id));
        removeById(id);
    }
}
