package com.travis.monolith.system.dict.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResult;
import com.travis.monolith.system.dict.api.response.SysDictItemResp;
import com.travis.monolith.system.dict.internal.entity.SysDict;
import com.travis.monolith.system.dict.internal.request.SysDictItemReq;
import com.travis.monolith.system.dict.internal.request.SysDictReq;
import java.util.List;

/**
 * 字典管理服务接口，提供字典类型及字典数据项的增删改查
 *
 * @author travis
 */
public interface SysDictService extends IService<SysDict> {

    /**
     * 获取字典树形数据（每个字典包含其下的数据项作为 children）
     *
     * @return 字典树形列表
     */
    List<SysDict> getDictTree();

    /**
     * 分页查询字典类型列表
     *
     * @param dictName 字典名称（模糊匹配，可为空）
     * @param dictType 字典类型编码（模糊匹配，可为空）
     * @param status 状态（可为空）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageResult<SysDict> getDictPage(
            String dictName, String dictType, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 获取字典类型详情
     *
     * @param id 字典ID
     * @return 字典实体
     */
    SysDict getDictDetail(Long id);

    /**
     * 新增字典类型
     *
     * @param req 字典信息请求参数
     */
    void addDict(SysDictReq req);

    /**
     * 更新字典类型
     *
     * @param id 字典ID
     * @param req 字典信息请求参数
     */
    void updateDict(Long id, SysDictReq req);

    /**
     * 删除字典类型
     *
     * @param id 字典ID
     */
    void deleteDict(Long id);

    /**
     * 查询指定字典类型下的所有数据项
     *
     * @param dictId 字典类型ID
     * @return 字典数据项视图列表
     */
    List<SysDictItemResp> getDictItems(Long dictId);

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
