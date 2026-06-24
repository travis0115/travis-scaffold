package com.travis.monolith.system.dict.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.monolith.system.dict.api.request.SysDictCreateReq;
import com.travis.monolith.system.dict.api.request.SysDictPageReq;
import com.travis.monolith.system.dict.api.request.SysDictUpdateReq;
import com.travis.monolith.system.dict.api.response.SysDictResp;
import com.travis.monolith.system.dict.internal.entity.SysDict;
import java.util.List;

/**
 * 字典管理服务接口，提供字典类型的增删改查
 *
 * @author travis
 */
public interface SysDictService extends IService<SysDict> {

    /**
     * 获取字典树形数据（每个字典包含其下的数据项作为 children）
     *
     * @return 字典树形列表
     */
    List<SysDictResp> listTree();

    /** 分页查询字典类型列表 */
    PageResp<SysDictResp> page(SysDictPageReq req);

    /**
     * 获取字典类型详情
     *
     * @param id 字典ID
     * @return 字典实体
     */
    SysDictResp getById(Long id);

    /**
     * 新增字典类型
     *
     * @param req 字典信息请求参数
     */
    void create(SysDictCreateReq req);

    /**
     * 更新字典类型
     *
     * @param id 字典ID
     * @param req 字典信息请求参数
     */
    void update(Long id, SysDictUpdateReq req);

    /** 修改字典类型状态 */
    void updateStatus(Long id, Integer status);

    /**
     * 删除字典类型
     *
     * @param id 字典ID
     */
    void deleteById(Long id);
}
