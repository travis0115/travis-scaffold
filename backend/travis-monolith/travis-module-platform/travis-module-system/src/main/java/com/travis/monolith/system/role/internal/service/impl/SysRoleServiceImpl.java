package com.travis.monolith.system.role.internal.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.monolith.system.common.api.enums.IsBuiltin;
import com.travis.monolith.system.common.api.enums.Modifiable;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import com.travis.monolith.system.role.api.request.SysRoleCreateReq;
import com.travis.monolith.system.role.api.request.SysRoleMenuReq;
import com.travis.monolith.system.role.api.request.SysRolePageReq;
import com.travis.monolith.system.role.api.request.SysRoleUpdateReq;
import com.travis.monolith.system.role.api.response.SysRoleResp;
import com.travis.monolith.system.role.internal.converter.SysRoleConverter;
import com.travis.monolith.system.role.internal.entity.SysRole;
import com.travis.monolith.system.role.internal.entity.SysRoleMenu;
import com.travis.monolith.system.role.internal.entity.SysUserRole;
import com.travis.monolith.system.role.internal.enums.Role;
import com.travis.monolith.system.role.internal.mapper.SysRoleMapper;
import com.travis.monolith.system.role.internal.mapper.SysRoleMenuMapper;
import com.travis.monolith.system.role.internal.mapper.SysUserRoleMapper;
import com.travis.monolith.system.role.internal.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理服务实现，包含角色-菜单关联、角色信息查询
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "system:role")
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole>
        implements SysRoleService {

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
                        .orderByAsc(SysRole::getId);
        var page = page(new Page<>(req.getPageNum(), req.getPageSize()), wrapper);
        return PageConverter.toResp(page.convert(converter::toResp));
    }

    /** 获取角色详情，同时查询角色关联的菜单ID列表 */
    @Override
    @Cacheable(key = "'id:'+#id")
    public SysRoleResp getById(Long id) {
        var role = getRoleOrThrow(id);
        var roleResp = converter.toResp(role);
        List<Long> menuIds =
                roleMenuMapper
                        .selectList(
                                new LambdaQueryWrapperX<SysRoleMenu>()
                                        .eq(SysRoleMenu::getRoleId, id))
                        .stream()
                        .map(SysRoleMenu::getMenuId)
                        .collect(Collectors.toList());
        roleResp.setMenuIds(menuIds);
        return roleResp;
    }

    /** 新增角色 */
    @Override
    @Transactional
    @CacheEvict(key = "'list:enabled'")
    public void create(SysRoleCreateReq req) {
        // 检查角色编码唯一性
        long count =
                count(
                        new LambdaQueryWrapperX<SysRole>()
                                .eq(SysRole::getRoleCode, req.getRoleCode()));
        if (count > 0) {
            throw new BizException(SystemErrorCode.ROLE_CODE_EXISTS);
        }
        var role = converter.toEntity(req);
        save(role);
    }

    /** 更新角色信息 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(key = "'id:'+#id"),
                @CacheEvict(key = "'code:'+#id"),
                @CacheEvict(key = "'name:'+#id"),
                @CacheEvict(key = "'list:enabled'")
            })
    public void update(Long id, SysRoleUpdateReq req) {
        var role = getRoleOrThrow(id);
        checkModifiable(role);
        // 检查角色编码唯一性（排除自身）
        if (req.getRoleCode() != null) {
            long count =
                    count(
                            new LambdaQueryWrapperX<SysRole>()
                                    .eq(SysRole::getRoleCode, req.getRoleCode())
                                    .ne(SysRole::getId, id));
            if (count > 0) {
                throw new BizException(SystemErrorCode.ROLE_CODE_EXISTS);
            }
        }
        converter.update(req, role);
        updateById(role);
    }

    /** 删除角色，同时清除角色-菜单和用户-角色关联 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(key = "'id:'+#id"),
                @CacheEvict(key = "'code:'+#id"),
                @CacheEvict(key = "'name:'+#id"),
                @CacheEvict(key = "'list:enabled'"),
                @CacheEvict(value = "system:user-role", allEntries = true),
                @CacheEvict(value = "system:role-menu", key = "'role:'+#id"),
                @CacheEvict(value = "system:menu:vben", allEntries = true)
            })
    public void deleteById(Long id) {
        var role = getRoleOrThrow(id);
        checkDeletable(role);
        removeById(id);
        // 删除角色-菜单关联
        roleMenuMapper.delete(
                new LambdaQueryWrapperX<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        // 删除用户-角色关联
        userRoleMapper.delete(
                new LambdaQueryWrapperX<SysUserRole>().eq(SysUserRole::getRoleId, id));
    }

    /** 分配角色菜单：先删除原有关联，再批量插入新关联，清除菜单缓存 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(key = "'id:'+#req.roleId"),
                @CacheEvict(value = "system:role-menu", key = "'role:'+#req.roleId"),
                @CacheEvict(value = "system:menu:vben", allEntries = true)
            })
    public void assignMenus(SysRoleMenuReq req) {
        var role = getRoleOrThrow(req.getRoleId());
        checkModifiable(role);
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
            roleMenuMapper.insert(list);
        }
    }

    /** 根据角色ID获取角色编码 */
    @Override
    @Cacheable(key = "'code:'+#roleId")
    public String getRoleCodeByRoleId(Long roleId) {
        if (roleId == null) {
            return null;
        }
        var role = super.getById(roleId);
        return role == null ? null : role.getRoleCode();
    }

    /** 根据角色ID获取角色名称 */
    @Override
    @Cacheable(key = "'name:'+#roleId")
    public String getRoleNameByRoleId(Long roleId) {
        if (roleId == null) {
            return null;
        }
        var role = super.getById(roleId);
        return role == null ? null : role.getRoleName();
    }

    /** 根据角色ID查询关联的菜单ID列表 */
    @Override
    @Cacheable(value = "system:role-menu", key = "'role:'+#roleId")
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        if (roleId == null) {
            return List.of();
        }
        return roleMenuMapper
                .selectList(
                        new LambdaQueryWrapperX<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());
    }

    /** 自动为所有 admin 角色分配指定菜单 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(allEntries = true),
                @CacheEvict(value = "system:role-menu", allEntries = true),
                @CacheEvict(value = "system:menu:vben", allEntries = true)
            })
    public void assignMenuToAdminRoles(Long menuId) {
        List<SysRole> adminRoles =
                list(
                        new LambdaQueryWrapperX<SysRole>()
                                .eq(SysRole::getRoleCode, Role.ADMIN.getValue()));
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

    /** 删除指定菜单的所有角色关联 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(allEntries = true),
                @CacheEvict(value = "system:role-menu", allEntries = true),
                @CacheEvict(value = "system:menu", allEntries = true)
            })
    public void removeMenuRelations(List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        roleMenuMapper.delete(
                new LambdaQueryWrapperX<SysRoleMenu>().in(SysRoleMenu::getMenuId, menuIds));
    }

    /** 获取所有启用角色列表（不分页） */
    @Override
    @Cacheable(key = "'list:enabled'")
    public List<SysRoleResp> listEnabled() {
        return converter.toRespList(
                list(
                        new LambdaQueryWrapperX<SysRole>()
                                .eq(SysRole::getStatus, Status.ENABLED.getValue())
                                .orderByAsc(SysRole::getCreateTime)));
    }

    /** 根据用户ID查询其角色ID列表 */
    @Override
    @Cacheable(value = "system:user-role", key = "'user:'+#userId")
    public List<Long> getRoleIdsByUserId(Long userId) {
        return userRoleMapper
                .selectList(
                        new LambdaQueryWrapperX<SysUserRole>().eq(SysUserRole::getUserId, userId))
                .stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "system:user-role", key = "'role:'+#roleId")
    public List<Long> getUserIdsByRoleId(Long roleId) {
        if (roleId == null) {
            return List.of();
        }
        return userRoleMapper
                .selectList(
                        new LambdaQueryWrapperX<SysUserRole>().eq(SysUserRole::getRoleId, roleId))
                .stream()
                .map(SysUserRole::getUserId)
                .distinct()
                .toList();
    }

    /** 删除指定用户的所有角色关联 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(value = "system:user-role", allEntries = true),
                @CacheEvict(value = "system:menu:vben", key = "#userId")
            })
    public void deleteUserRolesByUserId(Long userId) {
        userRoleMapper.delete(
                new LambdaQueryWrapperX<SysUserRole>().eq(SysUserRole::getUserId, userId));
    }

    /** 为指定用户分配角色 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(value = "system:user-role", allEntries = true),
                @CacheEvict(value = "system:menu:vben", key = "#userId")
            })
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
                            .toList();
            list.forEach(userRoleMapper::insert);
        }
    }

    private SysRole getRoleOrThrow(Long id) {
        SysRole role = super.getById(id);
        if (role == null) {
            throw new BizException(SystemErrorCode.ROLE_NOT_FOUND);
        }
        return role;
    }

    private void checkModifiable(SysRole role) {
        if (Modifiable.IMMUTABLE.getValue().equals(role.getModifiable())) {
            throw new BizException(SystemErrorCode.ROLE_NOT_MODIFIABLE);
        }
    }

    private void checkDeletable(SysRole role) {
        checkModifiable(role);
        if (IsBuiltin.BUILTIN.getValue().equals(role.getIsBuiltin())) {
            throw new BizException(SystemErrorCode.ROLE_BUILTIN_NOT_DELETABLE);
        }
    }
}
