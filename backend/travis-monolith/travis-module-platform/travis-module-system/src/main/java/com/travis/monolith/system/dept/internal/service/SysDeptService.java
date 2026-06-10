package com.travis.monolith.system.dept.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.monolith.system.dept.api.response.SysDeptResp;
import com.travis.monolith.system.dept.internal.entity.SysDept;
import com.travis.monolith.system.dept.internal.request.SysDeptPageReq;
import com.travis.monolith.system.dept.internal.request.SysDeptReq;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
    List<SysDeptResp> listTree();

    /**
     * 根据查询条件获取部门树形列表
     *
     * @param req 列表查询参数
     * @return 部门树
     */
    List<SysDeptResp> listTree(SysDeptPageReq req);

    /**
     * 根据部门ID列表批量获取部门名称映射
     *
     * @param ids 部门ID集合
     * @return 部门ID -> 部门名称的映射
     */
    Map<Long, String> getDeptNameMapByIds(Collection<Long> ids);

    /**
     * 获取部门详情
     *
     * @param id 部门ID
     * @return 部门详情视图
     */
    SysDeptResp getById(Long id);

    /**
     * 新增部门
     *
     * @param req 部门信息请求参数
     */
    void create(SysDeptReq req);

    /**
     * 更新部门信息
     *
     * @param id 部门ID
     * @param req 部门信息请求参数
     */
    void update(Long id, SysDeptReq req);

    /**
     * 删除部门（存在子部门时禁止删除）
     *
     * @param id 部门ID
     */
    void deleteById(Long id);

    /**
     * 根据部门ID查询部门名称
     *
     * @param deptId 部门ID
     * @return 部门名称，不存在返回 null
     */
    String getDeptNameById(Long deptId);
}
