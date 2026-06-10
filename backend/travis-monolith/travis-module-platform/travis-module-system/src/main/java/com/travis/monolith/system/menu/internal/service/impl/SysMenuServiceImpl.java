package com.travis.monolith.system.menu.internal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.common.web.exception.CommonErrorCode;
import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.infrastructure.framework.mybatis.core.LambdaQueryWrapperX;
import com.travis.infrastructure.framework.web.core.exception.BizException;
import com.travis.monolith.system.common.api.SystemErrorCode;
import com.travis.monolith.system.menu.api.response.SysMenuResp;
import com.travis.monolith.system.menu.api.response.VbenMenuResp;
import com.travis.monolith.system.menu.internal.converter.SysMenuConverter;
import com.travis.monolith.system.menu.internal.entity.SysMenu;
import com.travis.monolith.system.menu.internal.mapper.SysMenuMapper;
import com.travis.monolith.system.menu.internal.request.SysMenuReq;
import com.travis.monolith.system.menu.internal.service.SysMenuService;
import com.travis.monolith.system.role.api.SysRoleApi;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;

/**
 * 菜单管理服务实现，支持菜单树构建和前端 Vben 路由菜单生成
 *
 * @author travis
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu>
        implements SysMenuService {

    /** 角色 API */
    private final SysRoleApi roleApi;

    /** 对象转换器 */
    private final SysMenuConverter converter;

    /** 获取菜单树形列表（管理后台使用），按排序号升序排列 */
    @Override
    @Cacheable(value = "system:menu:tree", key = "'all'")
    public List<SysMenuResp> listTree() {
        List<SysMenu> allMenus =
                list(new LambdaQueryWrapperX<SysMenu>().orderByAsc(SysMenu::getSort));
        List<SysMenuResp> voList = converter.toRespList(allMenus);
        voList.forEach(v -> v.setChildren(new ArrayList<>()));
        return buildTree(voList);
    }

    /** 获取菜单详情 */
    @Override
    public SysMenuResp getById(Long id) {
        SysMenu menu = super.getById(id);
        if (menu == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        SysMenuResp resp = converter.toResp(menu);
        resp.setChildren(new ArrayList<>());
        return resp;
    }

    /** 新增菜单，新增后自动关联到所有 admin 角色 */
    @Override
    @Transactional
    @CacheEvict(
            value = {"system:menu:tree", "menus:vben"},
            key = "'all'",
            allEntries = true)
    public void create(SysMenuReq req) {
        SysMenu menu = converter.toEntity(req);
        save(menu);
        // 通过角色服务自动分配给 admin 角色
        roleApi.assignMenuToAdminRoles(menu.getId());
    }

    /** 更新菜单信息 */
    @Override
    @CacheEvict(
            value = {"system:menu:tree", "menus:vben"},
            key = "'all'",
            allEntries = true)
    public void update(Long id, SysMenuReq req) {
        SysMenu menu = super.getById(id);
        if (menu == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        converter.update(req, menu);
        updateById(menu);
    }

    /** 删除菜单，存在子菜单时禁止删除，删除时自动移除 admin 角色的关联记录 */
    @Override
    @Transactional
    @CacheEvict(
            value = {"system:menu:tree", "menus:vben"},
            key = "'all'",
            allEntries = true)
    public void deleteById(Long id) {
        long childCount = count(new LambdaQueryWrapperX<SysMenu>().eq(SysMenu::getParentId, id));
        if (childCount > 0) {
            throw new BizException(SystemErrorCode.SYSTEM_MENU_HAS_CHILDREN);
        }
        // 通过角色服务自动移除 admin 角色关联
        roleApi.removeMenuFromAdminRoles(id);
        removeById(id);
    }

    /** 上移菜单，与同级上一个菜单交换排序号 */
    @Override
    @Transactional
    @CacheEvict(
            value = {"system:menu:tree", "menus:vben"},
            key = "'all'",
            allEntries = true)
    public void moveUp(Long id) {
        SysMenu current = super.getById(id);
        if (current == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        // 查询同级所有菜单，按排序号升序
        List<SysMenu> siblings =
                list(
                        new LambdaQueryWrapperX<SysMenu>()
                                .eq(SysMenu::getParentId, current.getParentId())
                                .orderByAsc(SysMenu::getSort));
        int index = findIndex(siblings, id);
        if (index <= 0) {
            return;
        }
        swapSort(siblings.get(index), siblings.get(index - 1));
    }

    /** 下移菜单，与同级下一个菜单交换排序号 */
    @Override
    @Transactional
    @CacheEvict(
            value = {"system:menu:tree", "menus:vben"},
            key = "'all'",
            allEntries = true)
    public void moveDown(Long id) {
        SysMenu current = super.getById(id);
        if (current == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        // 查询同级所有菜单，按排序号升序
        List<SysMenu> siblings =
                list(
                        new LambdaQueryWrapperX<SysMenu>()
                                .eq(SysMenu::getParentId, current.getParentId())
                                .orderByAsc(SysMenu::getSort));
        int index = findIndex(siblings, id);
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
        Integer tempSort = a.getSort();
        a.setSort(b.getSort());
        b.setSort(tempSort);
        updateById(a);
        updateById(b);
    }

    /** 根据角色ID列表生成 Vben Admin 格式的菜单树，自动补充父级菜单确保树结构完整 */
    @Override
    public List<VbenMenuResp> getVbenMenuTree(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 根据角色ID列表查询关联的菜单ID
        List<Long> menuIds = roleApi.getMenuIdsByRoleIds(roleIds);

        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 只取目录（0）和菜单（1），不取按钮（2）
        List<SysMenu> menus =
                list(
                        new LambdaQueryWrapperX<SysMenu>()
                                .in(SysMenu::getId, menuIds)
                                .in(SysMenu::getMenuType, 0, 1)
                                .eq(SysMenu::getStatus, 1)
                                .orderByAsc(SysMenu::getSort));

        // 补充父级菜单，确保树结构完整（递归补充所有祖先节点）
        Set<Long> existingIds = menus.stream().map(SysMenu::getId).collect(Collectors.toSet());
        Set<Long> needParentIds =
                menus.stream()
                        .map(SysMenu::getParentId)
                        .filter(pid -> pid != 0 && !existingIds.contains(pid))
                        .collect(Collectors.toSet());
        while (!needParentIds.isEmpty()) {
            List<SysMenu> parents =
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
        Map<Long, List<SysMenu>> grouped =
                menus.stream().collect(Collectors.groupingBy(SysMenu::getParentId));
        List<VbenMenuResp> result =
                menus.stream()
                        .filter(m -> m.getParentId() == 0)
                        .map(m -> toVbenTree(m, grouped))
                        .collect(Collectors.toList());

        // 固定第一个叶子菜单（递归取第一个无子菜单的节点，标记 affixTab）
        affixFirstLeafMenu(result);

        return result;
    }

    /**
     * 递归找到菜单树中第一个叶子节点（无子菜单的具体页面），标记 affixTab=true 使其固定在标签栏
     *
     * @param menus 菜单列表
     * @return 是否已找到并标记
     */
    private boolean affixFirstLeafMenu(List<VbenMenuResp> menus) {
        if (menus == null || menus.isEmpty()) {
            return false;
        }
        for (VbenMenuResp menu : menus) {
            if (menu.getChildren() == null || menu.getChildren().isEmpty()) {
                // 叶子节点，标记固定
                if (menu.getMeta() != null) {
                    menu.getMeta().put("affixTab", true);
                }
                return true;
            }
            // 有子菜单，继续往下找
            if (affixFirstLeafMenu(menu.getChildren())) {
                return true;
            }
        }
        return false;
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
     * @param all 所有菜单视图列表
     * @return 顶层菜单树
     */
    private List<SysMenuResp> buildTree(List<SysMenuResp> all) {
        Map<Long, List<SysMenuResp>> grouped =
                all.stream().collect(Collectors.groupingBy(SysMenuResp::getParentId));
        all.forEach(
                node -> node.setChildren(grouped.getOrDefault(node.getId(), new ArrayList<>())));
        return all.stream().filter(node -> node.getParentId() == 0).collect(Collectors.toList());
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
