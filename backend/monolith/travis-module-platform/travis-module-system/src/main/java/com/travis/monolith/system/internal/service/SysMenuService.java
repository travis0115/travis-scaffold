package com.travis.monolith.system.internal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travis.monolith.system.internal.model.entity.SysMenu;
import com.travis.monolith.system.internal.model.req.SysMenuReq;
import com.travis.monolith.system.internal.model.resp.SysMenuResp;
import com.travis.monolith.system.internal.model.resp.VbenMenuResp;

import java.util.List;

/**
 * 菜单管理服务接口，提供菜单树查询、增删改查及前端路由菜单生成
 *
 * @author travis
 */
public interface SysMenuService extends IService<SysMenu> {

    /**
     * 获取菜单树形列表（管理后台使用）
     *
     * @return 菜单树
     */
    List<SysMenuResp> getMenuTree();

    /**
     * 获取菜单详情
     *
     * @param id 菜单ID
     * @return 菜单详情视图
     */
    SysMenuResp getMenuDetail(Long id);

    /**
     * 新增菜单
     *
     * @param req 菜单信息请求参数
     */
    void addMenu(SysMenuReq req);

    /**
     * 更新菜单信息
     *
     * @param id  菜单ID
     * @param req 菜单信息请求参数
     */
    void updateMenu(Long id, SysMenuReq req);

    /**
     * 删除菜单（存在子菜单时禁止删除）
     *
     * @param id 菜单ID
     */
    void deleteMenu(Long id);

    /**
     * 上移菜单（与同级上一个菜单交换排序号）
     *
     * @param id 菜单ID
     */
    void moveUp(Long id);

    /**
     * 下移菜单（与同级下一个菜单交换排序号）
     *
     * @param id 菜单ID
     */
    void moveDown(Long id);

    /**
     * 根据角色ID列表生成 Vben Admin 格式的菜单树（用于前端动态路由渲染）
     *
     * @param roleIds 角色ID列表
     * @return Vben Admin 格式的菜单树
     */
    List<VbenMenuResp> getVbenMenuTree(List<Long> roleIds);
}
