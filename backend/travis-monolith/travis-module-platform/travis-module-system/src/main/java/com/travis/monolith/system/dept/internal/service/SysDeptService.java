package com.travis.monolith.system.dept.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.monolith.system.dept.api.request.SysDeptCreateReq;
import com.travis.monolith.system.dept.api.request.SysDeptUpdateReq;
import com.travis.monolith.system.dept.api.response.SysDeptResp;
import com.travis.monolith.system.dept.internal.entity.SysDept;
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
     * 获取启用部门树形列表
     *
     * @return 启用部门树
     */
    List<SysDeptResp> listEnabledTree();

    /**
     * 获取部门详情
     *
     * @param id 部门ID
     * @return 部门详情视图
     */
    SysDeptResp getDetailByIdOrThrow(Long id);

    /**
     * 新增部门
     *
     * @param req 部门信息请求参数
     */
    void create(SysDeptCreateReq req);

    /**
     * 更新部门信息
     *
     * @param req 部门信息请求参数
     */
    void update(Long id, SysDeptUpdateReq req);

    /** 修改部门状态 */
    void updateStatus(Long id, Integer status);

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
    String getDeptNameByIdOrThrow(Long deptId);

    /** 批量查询部门 ID 与部门名称。 */
    Map<Long, String> getDeptNameMapByIds(Collection<Long> deptIds);

    /**
     * 判断指定部门中是否仍有任意一个存在
     *
     * @param deptIds 部门ID集合
     * @return 任意部门存在时返回 true
     */
    boolean existsAnyByIds(Collection<Long> deptIds);

    /** 获取指定部门及全部下级部门ID。 */
    List<Long> listSelfAndDescendantIds(Long deptId);
}
