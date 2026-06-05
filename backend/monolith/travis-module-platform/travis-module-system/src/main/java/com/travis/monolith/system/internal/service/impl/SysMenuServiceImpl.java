package com.travis.monolith.system.internal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.framework.jackson.core.util.JsonUtil;
import com.travis.infrastructure.framework.web.core.exception.BizException;
import com.travis.infrastructure.framework.web.core.exception.CommonErrorCode;
import com.travis.monolith.system.internal.converter.SysMenuConverter;
import com.travis.monolith.system.internal.exception.SystemErrorCode;
import com.travis.monolith.system.internal.mapper.SysMenuMapper;
import com.travis.monolith.system.internal.mapper.SysRoleMapper;
import com.travis.monolith.system.internal.mapper.SysRoleMenuMapper;
import com.travis.monolith.system.internal.model.entity.SysMenu;
import com.travis.monolith.system.internal.model.entity.SysRole;
import com.travis.monolith.system.internal.model.entity.SysRoleMenu;
import com.travis.monolith.system.internal.model.req.SysMenuReq;
import com.travis.monolith.system.internal.model.resp.SysMenuResp;
import com.travis.monolith.system.internal.model.resp.VbenMenuResp;
import com.travis.monolith.system.internal.service.SysMenuService;
import lombok.RequiredArgsConstructor;
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
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    /**
     * 角色-菜单关联 Mapper
     */
    private final SysRoleMenuMapper roleMenuMapper;
    /**
     * 角色 Mapper
     */
    private final SysRoleMapper roleMapper;
    /** 对象转换器 */
    private final SysMenuConverter converter;

    /**
     * 获取菜单树形列表（管理后台使用），按排序号升序排列
     */
    @Override
    @Cacheable(value = "system:menu:tree", key = "'all'")
    public List<SysMenuResp> getMenuTree() {
        List<SysMenu> allMenus = list(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getSort));
        List<SysMenuResp> voList = converter.toRespList(allMenus);
        voList.forEach(v -> v.setChildren(new ArrayList<>()));
        return buildTree(voList);
    }

    /**
     * 获取菜单详情
     */
    @Override
    public SysMenuResp getMenuDetail(Long id) {
        SysMenu menu = getById(id);
        if (menu == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        SysMenuResp resp = converter.toResp(menu);
        resp.setChildren(new ArrayList<>());
        return resp;
    }

    /**
     * 新增菜单，新增后自动关联到所有 admin 角色
     */
    @Override
    @Transactional
    @CacheEvict(value = {"system:menu:tree", "menus:vben"}, key = "'all'", allEntries = true)
    public void addMenu(SysMenuReq req) {
        SysMenu menu = converter.toEntity(req);
        save(menu);
        autoAssignToAdminRoles(menu.getId());
    }

    /**
     * 更新菜单信息
     */
    @Override
    @CacheEvict(value = {"system:menu:tree", "menus:vben"}, key = "'all'", allEntries = true)
    public void updateMenu(Long id, SysMenuReq req) {
        SysMenu menu = getById(id);
        if (menu == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        converter.update(req, menu);
        updateById(menu);
    }

    /**
     * 删除菜单，存在子菜单时禁止删除，删除时自动移除 admin 角色的关联记录
     */
    @Override
    @Transactional
    @CacheEvict(value = {"system:menu:tree", "menus:vben"}, key = "'all'", allEntries = true)
    public void deleteMenu(Long id) {
        long childCount = count(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, id));
        if (childCount > 0) {
            throw new BizException(SystemErrorCode.SYSTEM_MENU_HAS_CHILDREN);
        }
        autoRemoveFromAdminRoles(id);
        removeById(id);
    }

    /**
     * 上移菜单，与同级上一个菜单交换排序号
     */
    @Override
    @Transactional
    @CacheEvict(value = {"system:menu:tree", "menus:vben"}, key = "'all'", allEntries = true)
    public void moveUp(Long id) {
        SysMenu current = getById(id);
        if (current == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        // 查询同级所有菜单，按排序号升序
        List<SysMenu> siblings = list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, current.getParentId())
                .orderByAsc(SysMenu::getSort));
        int index = findIndex(siblings, id);
        if (index <= 0) {
            throw new BizException(SystemErrorCode.SYSTEM_MENU_ALREADY_TOP);
        }
        swapSort(siblings.get(index), siblings.get(index - 1));
    }

    /**
     * 下移菜单，与同级下一个菜单交换排序号
     */
    @Override
    @Transactional
    @CacheEvict(value = {"system:menu:tree", "menus:vben"}, key = "'all'", allEntries = true)
    public void moveDown(Long id) {
        SysMenu current = getById(id);
        if (current == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        // 查询同级所有菜单，按排序号升序
        List<SysMenu> siblings = list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, current.getParentId())
                .orderByAsc(SysMenu::getSort));
        int index = findIndex(siblings, id);
        if (index < 0 || index >= siblings.size() - 1) {
            throw new BizException(SystemErrorCode.SYSTEM_MENU_ALREADY_BOTTOM);
        }
        swapSort(siblings.get(index), siblings.get(index + 1));
    }

    /**
     * 查找菜单在同级列表中的索引位置
     */
    private int findIndex(List<SysMenu> siblings, Long id) {
        for (int i = 0; i < siblings.size(); i++) {
            if (siblings.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 交换两个菜单的排序号
     */
    private void swapSort(SysMenu a, SysMenu b) {
        Integer tempSort = a.getSort();
        a.setSort(b.getSort());
        b.setSort(tempSort);
        updateById(a);
        updateById(b);
    }

    /**
     * 根据角色ID列表生成 Vben Admin 格式的菜单树，自动补充父级菜单确保树结构完整
     */
    @Override
    public List<VbenMenuResp> getVbenMenuTree(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 根据角色ID列表查询关联的菜单ID
        List<Long> menuIds = roleMenuMapper.selectList(
                        new LambdaQueryWrapper<SysRoleMenu>()
                                .in(SysRoleMenu::getRoleId, roleIds))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .distinct()
                .collect(Collectors.toList());

        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 只取目录（0）和菜单（1），不取按钮（2）
        List<SysMenu> menus = list(new LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getId, menuIds)
                .in(SysMenu::getMenuType, 0, 1)
                .eq(SysMenu::getStatus, 1)
                .orderByAsc(SysMenu::getSort));

        // 补充父级菜单，确保树结构完整（递归补充所有祖先节点）
        Set<Long> existingIds = menus.stream().map(SysMenu::getId).collect(Collectors.toSet());
        Set<Long> needParentIds = menus.stream()
                .map(SysMenu::getParentId)
                .filter(pid -> pid != 0 && !existingIds.contains(pid))
                .collect(Collectors.toSet());
        while (!needParentIds.isEmpty()) {
            List<SysMenu> parents = list(new LambdaQueryWrapper<SysMenu>()
                    .in(SysMenu::getId, needParentIds)
                    .eq(SysMenu::getStatus, 1));
            menus.addAll(parents);
            existingIds.addAll(parents.stream().map(SysMenu::getId).collect(Collectors.toSet()));
            needParentIds = parents.stream()
                    .map(SysMenu::getParentId)
                    .filter(pid -> pid != 0 && !existingIds.contains(pid))
                    .collect(Collectors.toSet());
        }

        // 用 parentId 分组，构建树形 VbenMenuVO
        Map<Long, List<SysMenu>> grouped = menus.stream()
                .collect(Collectors.groupingBy(SysMenu::getParentId));
        List<VbenMenuResp> result = menus.stream()
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
     * @param menu    菜单实体
     * @param grouped 按 parentId 分组的菜单映射
     * @return Vben 菜单视图
     */
    private VbenMenuResp toVbenTree(SysMenu menu, Map<Long, List<SysMenu>> grouped) {
        List<SysMenu> children = grouped.getOrDefault(menu.getId(), Collections.emptyList());
        List<VbenMenuResp> childVOs = children.stream()
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
                Map<String, Object> extraMeta = JsonUtil.getObjectMapper().readValue(
                        menu.getMeta(), new TypeReference<LinkedHashMap<String, Object>>() {
                        });
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

    /**
     * 根据路由路径生成 PascalCase 路由名称（如 /system/user → SystemUser）
     */
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
        Map<Long, List<SysMenuResp>> grouped = all.stream()
                .collect(Collectors.groupingBy(SysMenuResp::getParentId));
        all.forEach(node -> node.setChildren(grouped.getOrDefault(node.getId(), new ArrayList<>())));
        return all.stream()
                .filter(node -> node.getParentId() == 0)
                .collect(Collectors.toList());
    }

    /**
     * 新增菜单后，自动为所有 role_code='admin' 的启用角色关联该菜单
     *
     * @param menuId 新增的菜单ID
     */
    private void autoAssignToAdminRoles(Long menuId) {
        List<SysRole> adminRoles = roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleCode, "admin")
                        .eq(SysRole::getStatus, 1));
        for (SysRole role : adminRoles) {
            long count = roleMenuMapper.selectCount(
                    new LambdaQueryWrapper<SysRoleMenu>()
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

    /**
     * 删除菜单后，自动移除所有 role_code='admin' 的角色对该菜单的关联记录
     *
     * @param menuId 被删除的菜单ID
     */
    private void autoRemoveFromAdminRoles(Long menuId) {
        List<Long> adminRoleIds = roleMapper.selectList(
                        new LambdaQueryWrapper<SysRole>()
                                .eq(SysRole::getRoleCode, "admin"))
                .stream().map(SysRole::getId).toList();
        if (!adminRoleIds.isEmpty()) {
            roleMenuMapper.delete(
                    new LambdaQueryWrapper<SysRoleMenu>()
                            .in(SysRoleMenu::getRoleId, adminRoleIds)
                            .eq(SysRoleMenu::getMenuId, menuId));
        }
    }
}
