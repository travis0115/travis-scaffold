package com.travis.monolith.system.dept.api;

import com.travis.monolith.system.dept.api.response.SysDeptResp;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 部门模块对外 API，供跨模块调用
 *
 * @author travis
 */
public interface SysDeptApi {

    /**
     * 获取部门树形列表
     *
     * @return 部门树
     */
    List<SysDeptResp> listTree();

    /**
     * 根据部门ID列表批量获取部门名称映射
     *
     * @param ids 部门ID集合
     * @return 部门ID -> 部门名称的映射
     */
    Map<Long, String> getDeptNameMapByIds(Collection<Long> ids);

    /**
     * 根据部门ID查询部门名称
     *
     * @param deptId 部门ID
     * @return 部门名称，不存在返回 null
     */
    String getDeptNameById(Long deptId);

    /**
     * 判断指定部门中是否仍有任意一个存在
     *
     * @param deptIds 部门ID集合
     * @return 任意部门存在时返回 true
     */
    boolean existsAnyByIds(Collection<Long> deptIds);

    /**
     * 获取指定部门及全部下级部门ID。
     *
     * @param deptId 部门ID
     * @return 部门ID集合
     */
    List<Long> listSelfAndDescendantIds(Long deptId);
}
