package com.travis.monolith.system.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.monolith.system.internal.model.entity.SysDept;
import com.travis.monolith.system.internal.model.request.dept.SysDeptReq;
import com.travis.monolith.system.internal.model.response.dept.SysDeptResp;
import java.util.List;

/**
 * 部门管理服务接口，提供部门树查询、增删改查
 *
 * @author travis
 */
public interface SysDeptService extends IService<SysDept> {

    /**
     * 获取部门树形列表
     *
     * @return 部门树
     */
    List<SysDeptResp> getDeptTree();

    /**
     * 获取部门详情
     *
     * @param id 部门ID
     * @return 部门详情视图
     */
    SysDeptResp getDeptDetail(Long id);

    /**
     * 新增部门
     *
     * @param req 部门信息请求参数
     */
    void addDept(SysDeptReq req);

    /**
     * 更新部门信息
     *
     * @param id 部门ID
     * @param req 部门信息请求参数
     */
    void updateDept(Long id, SysDeptReq req);

    /**
     * 删除部门（存在子部门时禁止删除）
     *
     * @param id 部门ID
     */
    void deleteDept(Long id);
}
