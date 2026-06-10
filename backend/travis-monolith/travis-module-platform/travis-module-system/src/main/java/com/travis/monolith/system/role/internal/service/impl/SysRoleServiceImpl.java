package com.travis.monolith.system.role.internal.service.impl;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.web.core.exception.BizException;
import com.travis.monolith.system.common.api.SystemErrorCode;
import com.travis.monolith.system.role.api.request.SysRoleMenuReq;
import com.travis.monolith.system.role.api.response.SysRoleResp;
import com.travis.monolith.system.role.internal.converter.SysRoleConverter;
import com.travis.monolith.system.role.internal.entity.SysRole;
import com.travis.monolith.system.role.internal.entity.SysRoleMenu;
import com.travis.monolith.system.role.internal.entity.SysUserRole;
import com.travis.monolith.system.role.internal.mapper.SysRoleMapper;
import com.travis.monolith.system.role.internal.mapper.SysRoleMenuMapper;
import com.travis.monolith.system.role.internal.mapper.SysUserRoleMapper;
import com.travis.monolith.system.role.internal.request.SysRolePageReq;
import com.travis.monolith.system.role.internal.request.SysRoleReq;
import com.travis.monolith.system.role.internal.service.SysRoleService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 角色管理服务实现，包含角色-菜单关联、角色信息查询
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole>
        implements SysRoleService {

    private static final Map<String, SFunction<SysRole, ?>> SORT_COLUMNS =
            Map.of(
                    "id", SysRole::getId,
                    "roleName", SysRole::getRoleName,
                    "roleCode", SysRole::getRoleCode,
                    "status", SysRole::getStatus,
                    "createTime", SysRole::getCreateTime,
                    "updateTime", SysRole::getUpdateTime);

    /** 角色-菜单关联 Mapper */
    private final SysRoleMenuMapper roleMenuMapper;

    /** 用户-角色关联 Mapper */
    private final SysUserRoleMapper userRoleMapper;

    /** 对象转换器 */
    private final SysRoleConverter converter;

    /** 分页查询角色列表，支持按角色名称、编码、状态筛选 */
    @Override
    public PageResp<SysRoleResp> page(SysRolePageReq req) {
        LambdaQueryWrapperX<SysRole> wrapper =
                new LambdaQueryWrapperX<SysRole>()
                        .likeIfPresent(SysRole::getRoleName, req.getRoleName())
                        .likeIfPresent(SysRole::getRoleCode, req.getRoleCode())
                        .eqIfPresent(SysRole::getStatus, req.getStatus())
                        .orderByAllowed(
                                req.getOrderBy(),
                                req.getAsc(),
                                SORT_COLUMNS,
                                false,
                                SysRole::getCreateTime);
        Page<SysRole> page = page(new Page<>(req.getPageNum(), req.getPageSize()), wrapper);
        return PageConverter.toResp(page.convert(converter::toResp));
    }

    /** 获取角色详情，同时查询角色关联的菜单ID列表 */
    @Override
    public SysRoleResp getById(Long id) {
        SysRole role = super.getById(id);
        if (role == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        SysRoleResp vo = converter.toResp(role);
        List<Long> menuIds =
                roleMenuMapper
                        .selectList(
                                new LambdaQueryWrapperX<SysRoleMenu>()
                                        .eq(SysRoleMenu::getRoleId, id))
                        .stream()
                        .map(SysRoleMenu::getMenuId)
                        .collect(Collectors.toList());
        vo.setMenuIds(menuIds);
        return vo;
    }

    /** 新增角色 */
    @Override
    @Transactional
    public void create(SysRoleReq req) {
        // 检查角色编码唯一性
        long count =
                count(
                        new LambdaQueryWrapperX<SysRole>()
                                .eq(SysRole::getRoleCode, req.getRoleCode()));
        if (count > 0) {
            throw new BizException(SystemErrorCode.SYSTEM_ROLE_CODE_EXISTS);
        }
        SysRole role = converter.toEntity(req);
        save(role);
    }

    /** 更新角色信息 */
    @Override
    @Transactional
    @CacheEvict(value = "menus:vben", allEntries = true)
    public void update(Long id, SysRoleReq req) {
        SysRole role = super.getById(id);
        if (role == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        // 检查角色编码唯一性（排除自身）
        if (req.getRoleCode() != null) {
            long count =
                    count(
                            new LambdaQueryWrapperX<SysRole>()
                                    .eq(SysRole::getRoleCode, req.getRoleCode())
                                    .ne(SysRole::getId, id));
            if (count > 0) {
                throw new BizException(SystemErrorCode.SYSTEM_ROLE_CODE_EXISTS);
            }
        }
        converter.update(req, role);
        updateById(role);
    }

    /** 删除角色，同时清除角色-菜单和用户-角色关联 */
    @Override
    @Transactional
    public void deleteById(Long id) {
        // 删除角色-菜单关联
        roleMenuMapper.delete(
                new LambdaQueryWrapperX<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        // 删除用户-角色关联
        userRoleMapper.delete(
                new LambdaQueryWrapperX<SysUserRole>().eq(SysUserRole::getRoleId, id));
        removeById(id);
    }

    /** 分配角色菜单：先删除原有关联，再批量插入新关联，清除菜单缓存 */
    @Override
    @Transactional
    @CacheEvict(value = "menus:vben", allEntries = true)
    public void assignMenus(SysRoleMenuReq req) {
        roleMenuMapper.delete(
                new LambdaQueryWrapperX<SysRoleMenu>().eq(SysRoleMenu::getRoleId, req.getRoleId()));
        if (req.getMenuIds() != null && !req.getMenuIds().isEmpty()) {
            List<SysRoleMenu> list =
                    req.getMenuIds().stream()
                            .map(
                                    menuId -> {
                                        SysRoleMenu rm = new SysRoleMenu();
                                        rm.setRoleId(req.getRoleId());
                                        rm.setMenuId(menuId);
                                        return rm;
                                    })
                            .toList();
            list.forEach(roleMenuMapper::insert);
        }
    }

    /** 根据角色ID列表获取角色编码列表 */
    @Override
    public List<String> getRoleCodesByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        return listByIds(roleIds).stream().map(SysRole::getRoleCode).collect(Collectors.toList());
    }

    /** 根据角色ID列表获取角色名称列表 */
    @Override
    public List<String> getRoleNamesByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        return listByIds(roleIds).stream().map(SysRole::getRoleName).collect(Collectors.toList());
    }

    /** 根据角色ID列表批量查询角色名称映射 */
    @Override
    public Map<Long, String> getRoleNameMapByIds(Set<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Map.of();
        }
        return listByIds(roleIds).stream()
                .collect(Collectors.toMap(SysRole::getId, SysRole::getRoleName));
    }

    /** 根据角色ID列表查询关联的菜单ID列表 */
    @Override
    public List<Long> getMenuIdsByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        return roleMenuMapper
                .selectList(
                        new LambdaQueryWrapperX<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .distinct()
                .collect(Collectors.toList());
    }

    /** 自动为所有 admin 角色分配指定菜单 */
    @Override
    @Transactional
    public void assignMenuToAdminRoles(Long menuId) {
        List<SysRole> adminRoles =
                list(
                        new LambdaQueryWrapperX<SysRole>()
                                .eq(SysRole::getRoleCode, "admin")
                                .eq(SysRole::getStatus, 1));
        for (SysRole role : adminRoles) {
            long count =
                    roleMenuMapper.selectCount(
                            new LambdaQueryWrapperX<SysRoleMenu>()
                                    .eq(SysRoleMenu::getRoleId, role.getId())
                                    .eq(SysRoleMenu::getMenuId, menuId));
            if (count == 0) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(role.getId());
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
            }
        }
    }

    /** 自动从所有 admin 角色移除指定菜单的关联 */
    @Override
    @Transactional
    public void removeMenuFromAdminRoles(Long menuId) {
        List<Long> adminRoleIds =
                list(new LambdaQueryWrapperX<SysRole>().eq(SysRole::getRoleCode, "admin")).stream()
                        .map(SysRole::getId)
                        .toList();
        if (!adminRoleIds.isEmpty()) {
            roleMenuMapper.delete(
                    new LambdaQueryWrapperX<SysRoleMenu>()
                            .in(SysRoleMenu::getRoleId, adminRoleIds)
                            .eq(SysRoleMenu::getMenuId, menuId));
        }
    }

    /** 获取所有启用角色列表（不分页） */
    @Override
    public List<SysRoleResp> listEnabled() {
        return converter.toRespList(
                list(
                        new LambdaQueryWrapperX<SysRole>()
                                .eq(SysRole::getStatus, 1)
                                .orderByAsc(SysRole::getCreateTime)));
    }

    /** 根据用户ID查询其角色ID列表 */
    @Override
    public List<Long> getRoleIdsByUserId(Long userId) {
        return userRoleMapper
                .selectList(
                        new LambdaQueryWrapperX<SysUserRole>().eq(SysUserRole::getUserId, userId))
                .stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
    }

    /** 根据用户ID查询角色编码列表 */
    @Override
    public List<String> getRoleCodesByUserId(Long userId) {
        List<Long> roleIds = getRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return listByIds(roleIds).stream().map(SysRole::getRoleCode).collect(Collectors.toList());
    }

    /** 根据用户ID查询角色名称列表 */
    @Override
    public List<String> getRoleNamesByUserId(Long userId) {
        List<Long> roleIds = getRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return listByIds(roleIds).stream().map(SysRole::getRoleName).collect(Collectors.toList());
    }

    /** 删除指定用户的所有角色关联 */
    @Override
    @Transactional
    public void deleteUserRolesByUserId(Long userId) {
        userRoleMapper.delete(
                new LambdaQueryWrapperX<SysUserRole>().eq(SysUserRole::getUserId, userId));
    }

    /** 为指定用户分配角色 */
    @Override
    @Transactional
    public void assignUserRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(
                new LambdaQueryWrapperX<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysUserRole> list =
                    roleIds.stream()
                            .map(
                                    roleId -> {
                                        SysUserRole ur = new SysUserRole();
                                        ur.setUserId(userId);
                                        ur.setRoleId(roleId);
                                        return ur;
                                    })
                            .collect(Collectors.toList());
            list.forEach(userRoleMapper::insert);
        }
    }

    /** 批量查询多个用户的角色名称映射 */
    @Override
    public Map<Long, List<String>> batchGetRoleNamesByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        List<SysUserRole> userRoles =
                userRoleMapper.selectList(
                        new LambdaQueryWrapperX<SysUserRole>().in(SysUserRole::getUserId, userIds));
        if (userRoles.isEmpty()) {
            return Map.of();
        }
        Set<Long> roleIds =
                userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toSet());
        Map<Long, String> roleNameMap = getRoleNameMapByIds(roleIds);
        return userRoles.stream()
                .collect(
                        Collectors.groupingBy(
                                SysUserRole::getUserId,
                                Collectors.mapping(
                                        ur -> roleNameMap.getOrDefault(ur.getRoleId(), "未知角色"),
                                        Collectors.toList())));
    }
}
