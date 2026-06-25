package com.travis.monolith.system.dict.internal.service.impl;

import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.infrastructure.framework.redis.core.RedisUtil;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.dict.api.request.SysDictItemCreateReq;
import com.travis.monolith.system.dict.api.request.SysDictItemUpdateReq;
import com.travis.monolith.system.dict.api.response.SysDictItemResp;
import com.travis.monolith.system.dict.internal.converter.SysDictItemConverter;
import com.travis.monolith.system.dict.internal.entity.SysDictItem;
import com.travis.monolith.system.dict.internal.mapper.SysDictItemMapper;
import com.travis.monolith.system.dict.internal.service.SysDictItemService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 字典数据项管理服务实现，处理字典子项的增删改查
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "system:dict:item")
public class SysDictItemServiceImpl extends ServiceImplX<SysDictItemMapper, SysDictItem>
        implements SysDictItemService {

    private final SysDictItemConverter converter;

    /** 分页查询字典数据项，按排序号升序 */
    @Override
    public PageResp<SysDictItem> page(Long dictId, Integer pageNum, Integer pageSize) {
        var wrapper =
                new LambdaQueryWrapperX<SysDictItem>()
                        .eq(dictId != null, SysDictItem::getDictId, dictId)
                        .orderByAsc(SysDictItem::getDictId)
                        .orderByAsc(SysDictItem::getSort);
        var page = page(pageNum, pageSize, wrapper);
        return PageConverter.toResp(page);
    }

    @Override
    @Cacheable(key = "'list:enabled'")
    public List<SysDictItem> listEnabled() {
        return list(
                new LambdaQueryWrapperX<SysDictItem>()
                        .eq(SysDictItem::getStatus, Status.ENABLED.getValue())
                        .orderByAsc(SysDictItem::getSort));
    }

    @Override
    @Cacheable(key = "'list:' + #dictId")
    public List<SysDictItemResp> listItemByDictId(Long dictId) {
        var items =
                list(
                        new LambdaQueryWrapperX<SysDictItem>()
                                .eq(SysDictItem::getDictId, dictId)
                                .orderByAsc(SysDictItem::getSort));
        return converter.toRespList(items);
    }

    /** 新增字典数据项 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(key = "'list:enabled'"),
                @CacheEvict(key = "'list:'+#req.getDictId()"),
                @CacheEvict(cacheNames = "system:dict", key = "'tree:enabled'")
            })
    public void create(SysDictItemCreateReq req) {
        save(converter.toEntity(req));
    }

    /** 更新字典数据项 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(key = "'list:enabled'"),
                @CacheEvict(cacheNames = "system:dict", key = "'tree:enabled'")
            })
    public void update(Long id, SysDictItemUpdateReq req) {
        var item = getByIdOrThrow(id);
        converter.update(req, item);
        updateById(item);
        RedisUtil.deleteCacheKey("system:dict:item", "list:" + item.getDictId());
    }

    /** 修改字典数据项状态 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(key = "'list:enabled'"),
                @CacheEvict(cacheNames = "system:dict", key = "'tree:enabled'")
            })
    public void updateStatus(Long id, Integer status) {
        var item = getByIdOrThrow(id);
        item.setStatus(status);
        updateById(item);
        RedisUtil.deleteCacheKey("system:dict:item", "list:" + item.getDictId());
    }

    /** 删除字典数据项 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(key = "'list:enabled'"),
                @CacheEvict(cacheNames = "system:dict", key = "'tree:enabled'")
            })
    public void deleteById(Long id) {
        var item = getByIdOrThrow(id);
        removeById(id);
        RedisUtil.deleteCacheKey("system:dict:item", "list:" + item.getDictId());
    }
}
