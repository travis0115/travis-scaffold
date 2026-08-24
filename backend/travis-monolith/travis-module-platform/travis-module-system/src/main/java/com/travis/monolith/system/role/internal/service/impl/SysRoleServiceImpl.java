package com.travis.monolith.system.role.internal.service.impl;

import com.travis.infrastructure.common.mapstruct.PageConverter;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.common.web.model.PageResp;
import com.travis.infrastructure.framework.event.core.TransactionalApplicationEventPublisher;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.mybatis.core.ServiceImplX;
import com.travis.monolith.system.common.api.BuiltinResourceGuard;
import com.travis.monolith.system.common.api.enums.IsBuiltin;
import com.travis.monolith.system.common.api.enums.Modifiable;
import com.travis.monolith.system.common.api.enums.Status;
import com.travis.monolith.system.common.api.enums.SystemErrorCode;
import com.travis.monolith.system.role.api.event.RoleMessageAudienceChangedEvent;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 角色管理服务实现，包含角色-菜单关联、角色信息查询
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "system:role")
public class SysRoleServiceImpl extends ServiceImplX<SysRoleMapper, SysRole>
        implements SysRoleService {

    /** 角色-菜单关联 Mapper */
    private final SysRoleMenuMapper roleMenuMapper;

    /** 用户-角色关联 Mapper */
    private final SysUserRoleMapper userRoleMapper;

    /** 对象转换器 */
    private final SysRoleConverter converter;

    private final BuiltinResourceGuard builtinResourceGuard;

    private final TransactionalApplicationEventPublisher eventPublisher;

    /** 分页查询角色列表，支持按角色名称、编码、状态筛选 */
    @Override
    public PageResp<SysRoleResp> page(SysRolePageReq req) {
        LambdaQueryWrapperX<SysRole> wrapper =
                new LambdaQueryWrapperX<SysRole>()
                        .likeIfPresent(SysRole::getRoleName, req.getRoleName())
                        .likeIfPresent(SysRole::getRoleCode, req.getRoleCode())
                        .eqIfPresent(SysRole::getStatus, req.getStatus())
                        .orderByAsc(SysRole::getCreateTime);
        var page = page(req.getPageNum(), req.getPageSize(), wrapper);
        return PageConverter.toResp(page.convert(converter::toResp));
    }

    /** 获取角色详情，同时查询角色关联的菜单ID列表 */
    @Override
    @Cacheable(key = "'detail:'+#id")
    public SysRoleResp getDetailByIdOrThrow(Long id) {
        var role = getByIdOrThrow(id);
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
                @CacheEvict(key = "'detail:'+#id"),
                @CacheEvict(key = "'code:'+#id"),
                @CacheEvict(key = "'name:'+#id"),
                @CacheEvict(key = "'list:enabled'"),
                @CacheEvict(value = "system:user:detail", allEntries = true)
            })
    public void update(Long id, SysRoleUpdateReq req) {
        var role = getByIdOrThrow(id);
        builtinResourceGuard.checkUpdate(role.getIsBuiltin());
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

    /** 修改角色状态 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(key = "'detail:'+#id"),
                @CacheEvict(key = "'code:'+#id"),
                @CacheEvict(key = "'list:enabled'"),
                @CacheEvict(value = "system:user-role", allEntries = true),
                @CacheEvict(value = "system:menu:tree:vben", allEntries = true)
            })
    public void updateStatus(Long id, Integer status) {
        var role = getByIdOrThrow(id);
        builtinResourceGuard.checkUpdate(role.getIsBuiltin());
        checkModifiable(role);
        role.setStatus(status);
        updateById(role);
        eventPublisher.publishEvent(new RoleMessageAudienceChangedEvent(null));
    }

    /** 删除角色，同时清除角色-菜单和用户-角色关联 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(key = "'detail:'+#id"),
                @CacheEvict(key = "'code:'+#id"),
                @CacheEvict(key = "'name:'+#id"),
                @CacheEvict(key = "'list:enabled'"),
                @CacheEvict(value = "system:user-role", allEntries = true),
                @CacheEvict(value = "system:role-menu", key = "'menu-ids:'+#id"),
                @CacheEvict(value = "system:menu:tree:vben", allEntries = true),
                @CacheEvict(value = "system:user:detail", allEntries = true)
            })
    public void deleteById(Long id) {
        var role = getByIdOrThrow(id);
        checkDeletable(role);
        removeById(id);
        // 删除角色-菜单关联
        roleMenuMapper.delete(
                new LambdaQueryWrapperX<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        // 删除用户-角色关联
        userRoleMapper.delete(
                new LambdaQueryWrapperX<SysUserRole>().eq(SysUserRole::getRoleId, id));
        eventPublisher.publishEvent(new RoleMessageAudienceChangedEvent(null));
    }

    /** 分配角色菜单：先删除原有关联，再批量插入新关联，清除菜单缓存 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(key = "'detail:'+#req.roleId"),
                @CacheEvict(value = "system:role-menu", key = "'menu-ids:'+#req.roleId"),
                @CacheEvict(value = "system:menu:tree:vben", allEntries = true)
            })
    public void assignMenus(SysRoleMenuReq req) {
        var role = baseMapper.selectByIdForUpdate(req.getRoleId());
        if (role == null) {
            throw new BizException(CommonErrorCode.DATABASE_RECORD_NOT_FOUND);
        }
        builtinResourceGuard.checkUpdate(role.getIsBuiltin());
        checkModifiable(role);
        List<Long> menuIds = normalizeIds(req.getMenuIds());
        if (!menuIds.isEmpty()
                && roleMenuMapper.selectExistingMenuIdsForUpdate(menuIds).size()
                        != menuIds.size()) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "存在无效的菜单ID");
        }
        roleMenuMapper.delete(
                new LambdaQueryWrapperX<SysRoleMenu>().eq(SysRoleMenu::getRoleId, req.getRoleId()));
        if (!menuIds.isEmpty()) {
            List<SysRoleMenu> list =
                    menuIds.stream()
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
        return role == null || !Status.ENABLED.getValue().equals(role.getStatus())
                ? null
                : role.getRoleCode();
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

    /** 批量查询角色名称，返回顺序与存在的输入角色 ID 一致。 */
    @Override
    public List<String> getRoleNamesByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        Map<Long, String> names =
                list(
                                new LambdaQueryWrapperX<SysRole>()
                                        .select(SysRole::getId, SysRole::getRoleName)
                                        .in(SysRole::getId, roleIds))
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        SysRole::getId,
                                        SysRole::getRoleName,
                                        (left, right) -> left));
        return roleIds.stream().map(names::get).filter(java.util.Objects::nonNull).toList();
    }

    /** 根据角色ID查询关联的菜单ID列表 */
    @Override
    @Cacheable(value = "system:role-menu", key = "'menu-ids:'+#roleId")
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
                @CacheEvict(value = "system:menu:tree:vben", allEntries = true)
            })
    public void assignMenuToAdminRoles(Long menuId) {
        List<SysRole> adminRoles =
                list(
                        new LambdaQueryWrapperX<SysRole>()
                                .eq(SysRole::getRoleCode, Role.ADMIN.getValue()));
        for (SysRole role : adminRoles) {
            boolean exists =
                    roleMenuMapper.exists(
                            new LambdaQueryWrapperX<SysRoleMenu>()
                                    .eq(SysRoleMenu::getRoleId, role.getId())
                                    .eq(SysRoleMenu::getMenuId, menuId));
            if (!exists) {
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
                @CacheEvict(value = "system:menu:tree:vben", allEntries = true)
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
    @Cacheable(value = "system:user-role", key = "'role-ids:'+#userId")
    public List<Long> getRoleIdsByUserId(Long userId) {
        return userRoleMapper
                .selectList(
                        new LambdaQueryWrapperX<SysUserRole>().eq(SysUserRole::getUserId, userId))
                .stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
    }

    /** 根据用户ID查询其启用角色ID列表 */
    @Override
    @Cacheable(value = "system:user-role", key = "'enabled-role-ids:'+#userId")
    public List<Long> getEnabledRoleIdsByUserId(Long userId) {
        List<Long> roleIds = getRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return list(
                        new LambdaQueryWrapperX<SysRole>()
                                .select(SysRole::getId)
                                .in(SysRole::getId, roleIds)
                                .eq(SysRole::getStatus, Status.ENABLED.getValue()))
                .stream()
                .map(SysRole::getId)
                .toList();
    }

    @Override
    public Map<Long, List<String>> getRoleNameMapByUserIds(List<Long> userIds) {
        List<Long> normalizedUserIds = normalizeIds(userIds);
        if (normalizedUserIds.isEmpty()) {
            return Map.of();
        }
        var userRoles =
                userRoleMapper.selectList(
                        new LambdaQueryWrapperX<SysUserRole>()
                                .select(SysUserRole::getUserId, SysUserRole::getRoleId)
                                .in(SysUserRole::getUserId, normalizedUserIds));
        var roleIds = userRoles.stream().map(SysUserRole::getRoleId).distinct().toList();
        Map<Long, String> roleNames =
                roleIds.isEmpty()
                        ? Map.of()
                        : list(
                                        new LambdaQueryWrapperX<SysRole>()
                                                .select(SysRole::getId, SysRole::getRoleName)
                                                .in(SysRole::getId, roleIds))
                                .stream()
                                .collect(
                                        Collectors.toMap(
                                                SysRole::getId,
                                                SysRole::getRoleName,
                                                (left, right) -> left));
        Map<Long, List<String>> result = new LinkedHashMap<>();
        normalizedUserIds.forEach(userId -> result.put(userId, new java.util.ArrayList<>()));
        userRoles.forEach(
                userRole -> {
                    var roleName = roleNames.get(userRole.getRoleId());
                    if (roleName != null) {
                        result.get(userRole.getUserId()).add(roleName);
                    }
                });
        return result;
    }

    /** 删除指定用户的所有角色关联 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(value = "system:user-role", allEntries = true),
                @CacheEvict(value = "system:menu:tree:vben", key = "#userId"),
                @CacheEvict(value = "system:user", key = "'detail:'+#userId")
            })
    public void deleteUserRolesByUserId(Long userId) {
        userRoleMapper.delete(
                new LambdaQueryWrapperX<SysUserRole>().eq(SysUserRole::getUserId, userId));
        eventPublisher.publishEvent(new RoleMessageAudienceChangedEvent(userId));
    }

    /** 为指定用户分配角色 */
    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(value = "system:user-role", allEntries = true),
                @CacheEvict(value = "system:menu:tree:vben", key = "#userId"),
                @CacheEvict(value = "system:user", key = "'detail:'+#userId")
            })
    public void assignUserRoles(Long userId, List<Long> roleIds) {
        List<Long> normalizedRoleIds = normalizeIds(roleIds);
        if (!normalizedRoleIds.isEmpty()
                && baseMapper.selectByIdsForUpdate(normalizedRoleIds).size()
                        != normalizedRoleIds.size()) {
            throw new BizException(CommonErrorCode.VALIDATE_FAILED, "存在无效的角色ID");
        }
        userRoleMapper.delete(
                new LambdaQueryWrapperX<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (!normalizedRoleIds.isEmpty()) {
            List<SysUserRole> list =
                    normalizedRoleIds.stream()
                            .map(
                                    roleId -> {
                                        SysUserRole ur = new SysUserRole();
                                        ur.setUserId(userId);
                                        ur.setRoleId(roleId);
                                        return ur;
                                    })
                            .toList();
            userRoleMapper.insert(list);
        }
        eventPublisher.publishEvent(new RoleMessageAudienceChangedEvent(userId));
    }

    /** 校验角色是否允许修改。 */
    private void checkModifiable(SysRole role) {
        if (Modifiable.NO.getValue().equals(role.getModifiable())) {
            throw new BizException(SystemErrorCode.ROLE_NOT_MODIFIABLE);
        }
    }

    /** 校验角色是否允许删除。 */
    private void checkDeletable(SysRole role) {
        checkModifiable(role);
        if (IsBuiltin.YES.getValue().equals(role.getIsBuiltin())) {
            throw new BizException(SystemErrorCode.ROLE_BUILTIN_NOT_DELETABLE);
        }
    }

    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return new LinkedHashSet<>(ids).stream().filter(java.util.Objects::nonNull).toList();
    }
}
