package com.travis.monolith.system.dict.internal.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.monolith.system.dict.api.request.SysDictItemCreateReq;
import com.travis.monolith.system.dict.api.request.SysDictItemUpdateReq;
import com.travis.monolith.system.dict.internal.converter.SysDictItemConverter;
import com.travis.monolith.system.dict.internal.entity.SysDictItem;
import com.travis.monolith.system.dict.internal.mapper.SysDictItemMapper;
import com.travis.monolith.system.dict.internal.service.SysDictItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 字典数据项管理服务实现，处理字典子项的增删改查
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
public class SysDictItemServiceImpl extends ServiceImpl<SysDictItemMapper, SysDictItem>
        implements SysDictItemService {

    private final SysDictItemConverter converter;

    /** 分页查询字典数据项，按排序号升序 */
    @Override
    public PageResp<SysDictItem> page(Long dictId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapperX<SysDictItem> wrapper =
                new LambdaQueryWrapperX<SysDictItem>()
                        .eq(dictId != null, SysDictItem::getDictId, dictId)
                        .orderByAsc(SysDictItem::getSort);
        Page<SysDictItem> page = page(new Page<>(pageNum, pageSize), wrapper);
        return PageConverter.toResp(page);
    }

    /** 新增字典数据项 */
    @Override
    public void create(SysDictItemCreateReq req) {
        save(converter.toEntity(req));
    }

    /** 更新字典数据项 */
    @Override
    public void update(Long id, SysDictItemUpdateReq req) {
        SysDictItem item = super.getById(id);
        if (item == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        converter.update(req, item);
        updateById(item);
    }

    /** 删除字典数据项 */
    @Override
    public void deleteById(Long id) {
        removeById(id);
    }
}
