package com.travis.monolith.system.internal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.framework.web.core.exception.BizException;
import com.travis.infrastructure.framework.web.core.exception.CommonErrorCode;
import com.travis.infrastructure.framework.web.core.model.PageResult;
import com.travis.monolith.system.internal.mapper.SysDictMapper;
import com.travis.monolith.system.internal.model.entity.SysDict;
import com.travis.monolith.system.internal.model.entity.SysDictItem;
import com.travis.monolith.system.internal.model.req.SysMenuReq;
import com.travis.monolith.system.internal.model.resp.SysDictItemResp;
import com.travis.monolith.system.internal.service.SysDictItemService;
import com.travis.monolith.system.internal.service.SysDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
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
    public void addDict(SysMenuReq.SysDictReq req) {
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
    public void updateDict(Long id, SysMenuReq.SysDictReq req) {
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
     * 删除字典类型
     */
    @Override
    public void deleteDict(Long id) {
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
        return items.stream()
                .map(item -> SysDictItemResp.builder()
                        .id(item.getId())
                        .dictId(item.getDictId())
                        .label(item.getLabel())
                        .value(item.getValue())
                        .sort(item.getSort())
                        .status(item.getStatus())
                        .remark(item.getRemark())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 新增字典数据项（委托给 {@link SysDictItemService}）
     */
    @Override
    public void addDictItem(SysMenuReq.SysDictItemReq req) {
        dictItemService.addDictItem(req);
    }

    /**
     * 更新字典数据项（委托给 {@link SysDictItemService}）
     */
    @Override
    public void updateDictItem(Long id, SysMenuReq.SysDictItemReq req) {
        dictItemService.updateDictItem(id, req);
    }

    /**
     * 删除字典数据项（委托给 {@link SysDictItemService}）
     */
    @Override
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
