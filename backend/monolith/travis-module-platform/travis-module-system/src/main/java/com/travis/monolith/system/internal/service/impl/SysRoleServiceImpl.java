package com.travis.monolith.system.internal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.framework.web.core.exception.BizException;
import com.travis.infrastructure.framework.web.core.exception.CommonErrorCode;
import com.travis.infrastructure.framework.web.core.model.PageResult;
import com.travis.monolith.system.internal.mapper.SysRoleMapper;
import com.travis.monolith.system.internal.mapper.SysRoleMenuMapper;
import com.travis.monolith.system.internal.mapper.SysUserRoleMapper;
import com.travis.monolith.system.internal.model.entity.SysRole;
import com.travis.monolith.system.internal.model.entity.SysRoleMenu;
import com.travis.monolith.system.internal.model.entity.SysUserRole;
import com.travis.monolith.system.internal.model.req.SysRoleMenuReq;
import com.travis.monolith.system.internal.model.req.SysRoleReq;
import com.travis.monolith.system.internal.model.resp.SysRoleResp;
import com.travis.monolith.system.internal.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理服务实现，包含角色-菜单关联、用户-角色关联查询
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    /** 角色-菜单关联 Mapper */
    private final SysRoleMenuMapper roleMenuMapper;
    /** 用户-角色关联 Mapper */
    private final SysUserRoleMapper userRoleMapper;

    /**
     * 分页查询角色列表，支持按角色名称、编码、状态筛选
     */
    @Override
    public PageResult<SysRoleResp> getRolePage(String roleName, String roleCode, Integer status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .like(roleName != null, SysRole::getRoleName, roleName)
                .like(roleCode != null, SysRole::getRoleCode, roleCode)
                .eq(status != null, SysRole::getStatus, status)
                .orderByDesc(SysRole::getCreateTime);
        Page<SysRole> page = page(new Page<>(pageNum, pageSize), wrapper);
        List<SysRoleResp> voList = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(voList, page.getTotal(), (int) page.getCurrent(), (int) page.getSize(), (int) page.getPages());
    }

    /**
     * 获取角色详情，同时查询角色关联的菜单ID列表
     */
    @Override
    public SysRoleResp getRoleDetail(Long id) {
        SysRole role = getById(id);
        if (role == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        SysRoleResp vo = toVO(role);
        List<Long> menuIds = roleMenuMapper.selectList(
                        new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id))
                .stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
        vo.setMenuIds(menuIds);
        return vo;
    }

    /**
     * 新增角色
     */
    @Override
    public void addRole(SysRoleReq req) {
        SysRole role = new SysRole();
        role.setRoleName(req.getRoleName());
        role.setRoleCode(req.getRoleCode());
        role.setRemark(req.getRemark());
        role.setModifiable(req.getModifiable());
        role.setStatus(req.getStatus());
        save(role);
    }

    /**
     * 更新角色信息
     */
    @Override
    public void updateRole(Long id, SysRoleReq req) {
        SysRole role = getById(id);
        if (role == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        role.setRoleName(req.getRoleName());
        role.setRoleCode(req.getRoleCode());
        role.setRemark(req.getRemark());
        role.setModifiable(req.getModifiable());
        role.setStatus(req.getStatus());
        updateById(role);
    }

    /**
     * 删除角色
     */
    @Override
    public void deleteRole(Long id) {
        removeById(id);
    }

    /**
     * 分配角色菜单：先删除原有关联，再批量插入新关联
     */
    @Override
    @Transactional
    public void assignMenus(SysRoleMenuReq req) {
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, req.getRoleId()));
        if (req.getMenuIds() != null && !req.getMenuIds().isEmpty()) {
            List<SysRoleMenu> list = req.getMenuIds().stream().map(menuId -> {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(req.getRoleId());
                rm.setMenuId(menuId);
                return rm;
            }).collect(Collectors.toList());
            list.forEach(roleMenuMapper::insert);
        }
    }

    /**
     * 根据用户ID查询其角色编码列表
     */
    @Override
    public List<String> getRoleCodesByUserId(Long userId) {
        List<Long> roleIds = getRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return listByIds(roleIds).stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());
    }

    /**
     * 根据用户ID查询其角色ID列表
     */
    @Override
    public List<Long> getRoleIdsByUserId(Long userId) {
        return userRoleMapper.selectList(
                        new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId))
                .stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
    }

    /**
     * 实体转视图对象
     *
     * @param role 角色实体
     * @return 角色视图对象
     */
    private SysRoleResp toVO(SysRole role) {
        return SysRoleResp.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .roleCode(role.getRoleCode())
                .remark(role.getRemark())
                .modifiable(role.getModifiable())
                .status(role.getStatus())
                .createTime(role.getCreateTime())
                .build();
    }
}
