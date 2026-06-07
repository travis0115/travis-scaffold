package com.travis.monolith.system.dict.internal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.framework.web.core.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResult;
import com.travis.monolith.system.dict.internal.mapper.SysDictItemMapper;
import com.travis.monolith.system.dict.internal.model.entity.SysDictItem;
import com.travis.monolith.system.dict.internal.model.request.SysDictItemReq;
import com.travis.monolith.system.dict.api.SysDictItemService;
import org.springframework.stereotype.Service;

/**
 * 字典数据项管理服务实现，处理字典子项的增删改查
 *
 * @author travis
 */
@Service
public class SysDictItemServiceImpl extends ServiceImpl<SysDictItemMapper, SysDictItem>
        implements SysDictItemService {

    /** 分页查询字典数据项，按排序号升序 */
    @Override
    public PageResult<SysDictItem> getDictItemPage(Long dictId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SysDictItem> wrapper =
                new LambdaQueryWrapper<SysDictItem>()
                        .eq(dictId != null, SysDictItem::getDictId, dictId)
                        .orderByAsc(SysDictItem::getSort);
        Page<SysDictItem> page = page(new Page<>(pageNum, pageSize), wrapper);
        return new PageResult<>(
                page.getRecords(),
                page.getTotal(),
                (int) page.getCurrent(),
                (int) page.getSize(),
                (int) page.getPages());
    }

    /** 新增字典数据项 */
    @Override
    public void addDictItem(SysDictItemReq req) {
        SysDictItem item = new SysDictItem();
        item.setDictId(req.getDictId());
        item.setLabel(req.getLabel());
        item.setValue(req.getValue());
        item.setSort(req.getSort());
        item.setStatus(req.getStatus());
        item.setRemark(req.getRemark());
        save(item);
    }

    /** 更新字典数据项 */
    @Override
    public void updateDictItem(Long id, SysDictItemReq req) {
        SysDictItem item = getById(id);
        if (item == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        item.setDictId(req.getDictId());
        item.setLabel(req.getLabel());
        item.setValue(req.getValue());
        item.setSort(req.getSort());
        item.setStatus(req.getStatus());
        item.setRemark(req.getRemark());
        updateById(item);
    }

    /** 删除字典数据项 */
    @Override
    public void deleteDictItem(Long id) {
        removeById(id);
    }
}
