package com.travis.monolith.system.menu.internal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.monolith.system.common.api.SystemErrorCode;
import com.travis.monolith.system.menu.api.request.SysMenuCreateReq;
import com.travis.monolith.system.menu.api.request.SysMenuUpdateReq;
import com.travis.monolith.system.menu.api.response.SysMenuResp;
import com.travis.monolith.system.menu.api.response.VbenMenuResp;
import com.travis.monolith.system.menu.internal.converter.SysMenuConverter;
import com.travis.monolith.system.menu.internal.entity.SysMenu;
import com.travis.monolith.system.menu.internal.mapper.SysMenuMapper;
import com.travis.monolith.system.menu.internal.service.SysMenuService;
import com.travis.monolith.system.role.api.SysRoleApi;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单管理服务实现，支持菜单树构建和前端 Vben 路由菜单生成
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "sys_menu")
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu>
        implements SysMenuService {

    /** 角色 API */
    private final SysRoleApi roleApi;

    /** 对象转换器 */
    private final SysMenuConverter converter;

    /** 获取菜单树形列表（管理后台使用），按排序号升序排列 */
    @Override
    @Cacheable(key = "'tree:all'")
    public List<SysMenuResp> listTree() {
        var menuList = list(new LambdaQueryWrapperX<SysMenu>().orderByAsc(SysMenu::getSort));
        var menuRespList = converter.toRespList(menuList);
        return buildTree(menuRespList);
    }

    /** 获取菜单详情 */
    @Override
    @Cacheable(key = "'id:' + #id")
    public SysMenuResp getById(Long id) {
        var menu = super.getById(id);
        if (menu == null) {
            throw new BizException(SystemErrorCode.MENU_NOT_FOUND);
        }
        return converter.toResp(menu);
    }

    /** 新增菜单 新增后自动关联到所有 admin 角色 */
    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void create(SysMenuCreateReq req) {
        validatePathUnique(req.getPath(), null);
        var menu = converter.toEntity(req);
        save(menu);
        // 通过角色服务自动分配给 admin 角色
        roleApi.assignMenuToAdminRoles(menu.getId());
    }

    /** 更新菜单信息 */
    @Override
    @CacheEvict(allEntries = true)
    @Transactional
    public void update(Long id, SysMenuUpdateReq req) {
        var menu = super.getById(id);
        if (menu == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        validatePathUnique(req.getPath(), id);
        converter.update(req, menu);
        updateById(menu);
    }

    /** 校验非空路由路径唯一性 */
    private void validatePathUnique(String path, Long excludeId) {
        if (path == null || path.isBlank()) {
            return;
        }
        var query = new LambdaQueryWrapperX<SysMenu>().eq(SysMenu::getPath, path);
        if (excludeId != null) {
            query.ne(SysMenu::getId, excludeId);
        }
        if (count(query) > 0) {
            throw new BizException(SystemErrorCode.MENU_PATH_EXISTS);
        }
    }

    /** 删除菜单及其所有子菜单，并清理角色菜单关联 */
    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void deleteById(Long id) {
        if (super.getById(id) == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        var menuIds = getMenuAndDescendantIds(id);
        removeByIds(menuIds);
        roleApi.removeMenuRelations(menuIds);
    }

    /** 获取指定菜单及其全部后代菜单ID */
    private List<Long> getMenuAndDescendantIds(Long id) {
        var childrenByParentId =
                list().stream()
                        .collect(
                                Collectors.groupingBy(
                                        SysMenu::getParentId,
                                        Collectors.mapping(SysMenu::getId, Collectors.toList())));
        var menuIds = new LinkedHashSet<Long>();
        var pendingIds = new ArrayDeque<Long>();
        pendingIds.add(id);
        while (!pendingIds.isEmpty()) {
            var currentId = pendingIds.removeFirst();
            if (menuIds.add(currentId)) {
                pendingIds.addAll(childrenByParentId.getOrDefault(currentId, List.of()));
            }
        }
        return new ArrayList<>(menuIds);
    }

    /** 上移菜单，与同级上一个菜单交换排序号 */
    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void moveUp(Long id) {
        var current = super.getById(id);
        if (current == null) {
            throw new BizException(SystemErrorCode.MENU_NOT_FOUND);
        }
        // 查询同级所有菜单，按排序号升序
        var siblings =
                list(
                        new LambdaQueryWrapperX<SysMenu>()
                                .eq(SysMenu::getParentId, current.getParentId())
                                .orderByAsc(SysMenu::getSort));
        var index = findIndex(siblings, id);
        if (index <= 0) {
            return;
        }
        swapSort(siblings.get(index), siblings.get(index - 1));
    }

    /** 下移菜单，与同级下一个菜单交换排序号 */
    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public void moveDown(Long id) {
        SysMenu current = super.getById(id);
        if (current == null) {
            throw new BizException(SystemErrorCode.MENU_NOT_FOUND);
        }
        // 查询同级所有菜单，按排序号升序
        var siblings =
                list(
                        new LambdaQueryWrapperX<SysMenu>()
                                .eq(SysMenu::getParentId, current.getParentId())
                                .orderByAsc(SysMenu::getSort));
        var index = findIndex(siblings, id);
        if (index < 0 || index >= siblings.size() - 1) {
            return;
        }
        swapSort(siblings.get(index), siblings.get(index + 1));
    }

    /** 查找菜单在同级列表中的索引位置 */
    private int findIndex(List<SysMenu> siblings, Long id) {
        for (int i = 0; i < siblings.size(); i++) {
            if (siblings.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    /** 交换两个菜单的排序号 */
    private void swapSort(SysMenu a, SysMenu b) {
        var tempSort = a.getSort();
        a.setSort(b.getSort());
        b.setSort(tempSort);
        updateById(a);
        updateById(b);
    }

    /** 根据角色ID列表生成 Vben Admin 格式的菜单树，自动补充父级菜单确保树结构完整 */
    @Override
    @Cacheable(key = "'vben:'+#userId")
    public List<VbenMenuResp> getVbenMenuTree(Long userId) {
        var roleIds = roleApi.getRoleIdsByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 根据角色ID列表查询关联的菜单ID
        var menuIds = roleApi.getMenuIdsByRoleIds(roleIds);

        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 只取目录（0）和菜单（1），不取按钮（2）
        var menus =
                list(
                        new LambdaQueryWrapperX<SysMenu>()
                                .in(SysMenu::getId, menuIds)
                                .in(SysMenu::getMenuType, 0, 1)
                                .eq(SysMenu::getStatus, 1)
                                .orderByAsc(SysMenu::getSort));

        // 补充父级菜单，确保树结构完整（递归补充所有祖先节点）
        var existingIds = menus.stream().map(SysMenu::getId).collect(Collectors.toSet());
        var needParentIds =
                menus.stream()
                        .map(SysMenu::getParentId)
                        .filter(pid -> pid != 0 && !existingIds.contains(pid))
                        .collect(Collectors.toSet());
        while (!needParentIds.isEmpty()) {
            var parents =
                    list(
                            new LambdaQueryWrapperX<SysMenu>()
                                    .in(SysMenu::getId, needParentIds)
                                    .eq(SysMenu::getStatus, 1));
            menus.addAll(parents);
            existingIds.addAll(parents.stream().map(SysMenu::getId).collect(Collectors.toSet()));
            needParentIds =
                    parents.stream()
                            .map(SysMenu::getParentId)
                            .filter(pid -> pid != 0 && !existingIds.contains(pid))
                            .collect(Collectors.toSet());
        }

        // 用 parentId 分组，构建树形 VbenMenuVO
        var grouped =
                menus.stream().collect(Collectors.groupingBy(SysMenu::getParentId));

        return menus.stream()
            .filter(m -> m.getParentId() == 0)
            .map(m -> toVbenTree(m, grouped))
            .collect(Collectors.toList());
    }

    /**
     * 递归构建 Vben Admin 菜单树，一级目录使用 BasicLayout 组件
     *
     * @param menu 菜单实体
     * @param grouped 按 parentId 分组的菜单映射
     * @return Vben 菜单视图
     */
    private VbenMenuResp toVbenTree(SysMenu menu, Map<Long, List<SysMenu>> grouped) {
        List<SysMenu> children = grouped.getOrDefault(menu.getId(), Collections.emptyList());
        List<VbenMenuResp> childVOs =
                children.stream()
                        .map(child -> toVbenTree(child, grouped))
                        .collect(Collectors.toList());

        // 一级目录（parentId=0 且有子菜单）使用 BasicLayout
        String component = menu.getComponent();
        if (menu.getParentId() == 0 && !children.isEmpty()) {
            component = "BasicLayout";
        }

        // 构建基础 meta：DB 独立字段
        Map<String, Object> metaMap = new LinkedHashMap<>();
        metaMap.put("title", menu.getMenuName());
        metaMap.put("order", menu.getSort());
        if (menu.getIcon() != null && !menu.getIcon().isEmpty()) {
            metaMap.put("icon", menu.getIcon());
        }

        // 合并 meta JSON 中的扩展字段
        if (menu.getMeta() != null && !menu.getMeta().isBlank()) {
            try {
                Map<String, Object> extraMeta =
                        JsonUtil.getObjectMapper()
                                .readValue(
                                        menu.getMeta(),
                                        new TypeReference<LinkedHashMap<String, Object>>() {});
                // 扩展字段覆盖基础字段（以 JSON 中的值为优先）
                extraMeta.forEach(metaMap::putIfAbsent);
            } catch (Exception e) {
                // JSON 解析失败时忽略，不影响菜单树构建
            }
        }

        return VbenMenuResp.builder()
                .name(generateRouteName(menu.getPath()))
                .path(menu.getPath())
                .component(component)
                .redirect(children.isEmpty() ? null : children.get(0).getPath())
                .meta(metaMap)
                .children(childVOs.isEmpty() ? null : childVOs)
                .build();
    }

    /** 根据路由路径生成 PascalCase 路由名称（如 /system/user → SystemUser） */
    private String generateRouteName(String path) {
        if (path == null || path.isEmpty()) {
            return "Route";
        }
        String[] segments = path.split("/");
        StringBuilder sb = new StringBuilder();
        for (String segment : segments) {
            if (segment.isEmpty()) continue;
            sb.append(Character.toUpperCase(segment.charAt(0)));
            if (segment.length() > 1) {
                sb.append(segment.substring(1));
            }
        }
        return sb.toString();
    }

    /**
     * 根据 parentId 分组构建菜单树，返回顶层节点（parentId=0）
     *
     * @param menuRespList 所有菜单视图列表
     * @return 顶层菜单树
     */
    private List<SysMenuResp> buildTree(List<SysMenuResp> menuRespList) {
        var grouped =
                menuRespList.stream().collect(Collectors.groupingBy(SysMenuResp::getParentId));
        menuRespList.forEach(
                node -> node.setChildren(grouped.getOrDefault(node.getId(), new ArrayList<>())));
        return menuRespList.stream()
                .filter(node -> node.getParentId() == 0)
                .collect(Collectors.toList());
    }

    /** 根据菜单ID列表查询已启用菜单的权限标识列表 */
    @Override
    public List<String> getPermissionsByMenuIds(List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return List.of();
        }
        return list(
                        new LambdaQueryWrapperX<SysMenu>()
                                .in(SysMenu::getId, menuIds)
                                .isNotNull(SysMenu::getPerms)
                                .ne(SysMenu::getPerms, "")
                                .eq(SysMenu::getStatus, 1))
                .stream()
                .map(SysMenu::getPerms)
                .distinct()
                .collect(Collectors.toList());
    }
}
