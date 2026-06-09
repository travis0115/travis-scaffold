package com.travis.monolith.system.dict.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResult;
import com.travis.monolith.system.dict.internal.entity.SysDictItem;
import com.travis.monolith.system.dict.internal.request.SysDictItemReq;

/**
 * 字典数据项管理服务接口，提供字典子项的分页查询和增删改
 *
 * @author travis
 */
public interface SysDictItemService extends IService<SysDictItem> {

    /**
     * 分页查询字典数据项
     *
     * @param dictId 所属字典类型ID（可为空）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResult<SysDictItem> getDictItemPage(Long dictId, Integer pageNum, Integer pageSize);

    /**
     * 新增字典数据项
     *
     * @param req 字典数据项请求参数
     */
    void addDictItem(SysDictItemReq req);

    /**
     * 更新字典数据项
     *
     * @param id 数据项ID
     * @param req 字典数据项请求参数
     */
    void updateDictItem(Long id, SysDictItemReq req);

    /**
     * 删除字典数据项
     *
     * @param id 数据项ID
     */
    void deleteDictItem(Long id);
}
