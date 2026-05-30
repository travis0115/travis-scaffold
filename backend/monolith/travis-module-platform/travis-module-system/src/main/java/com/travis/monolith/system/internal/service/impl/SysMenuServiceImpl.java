package com.travis.monolith.system.internal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travis.infrastructure.framework.web.core.exception.BizException;
import com.travis.infrastructure.framework.web.core.exception.CommonErrorCode;
import com.travis.infrastructure.framework.web.core.exception.IErrorCode;
import com.travis.monolith.system.internal.mapper.SysMenuMapper;
import com.travis.monolith.system.internal.mapper.SysRoleMenuMapper;
import com.travis.monolith.system.internal.model.entity.SysMenu;
import com.travis.monolith.system.internal.model.entity.SysRoleMenu;
import com.travis.monolith.system.internal.model.req.SysMenuReq;
import com.travis.monolith.system.internal.model.resp.SysMenuResp;
import com.travis.monolith.system.internal.model.resp.VbenMenuResp;
import com.travis.monolith.system.internal.service.SysMenuService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    /** 角色-菜单关联 Mapper */
    private final SysRoleMenuMapper roleMenuMapper;

    /**
     * 获取菜单树形列表（管理后台使用），按排序号升序排列
     */
    @Override
    public List<SysMenuResp> getMenuTree() {
        List<SysMenu> allMenus = list(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getSort));
        List<SysMenuResp> voList = allMenus.stream().map(this::toVO).collect(Collectors.toList());
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
        return toVO(menu);
    }

    /**
     * 新增菜单
     */
    @Override
    public void addMenu(SysMenuReq req) {
        SysMenu menu = new SysMenu();
        menu.setParentId(req.getParentId());
        menu.setName(req.getName());
        menu.setPath(req.getPath());
        menu.setComponent(req.getComponent());
        menu.setPerms(req.getPerms());
        menu.setMenuType(req.getMenuType());
        menu.setIcon(req.getIcon());
        menu.setSort(req.getSort());
        menu.setStatus(req.getStatus());
        save(menu);
    }

    /**
     * 更新菜单信息
     */
    @Override
    public void updateMenu(Long id, SysMenuReq req) {
        SysMenu menu = getById(id);
        if (menu == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND);
        }
        menu.setParentId(req.getParentId());
        menu.setName(req.getName());
        menu.setPath(req.getPath());
        menu.setComponent(req.getComponent());
        menu.setPerms(req.getPerms());
        menu.setMenuType(req.getMenuType());
        menu.setIcon(req.getIcon());
        menu.setSort(req.getSort());
        menu.setStatus(req.getStatus());
        updateById(menu);
    }

    /**
     * 删除菜单，存在子菜单时禁止删除
     */
    @Override
    public void deleteMenu(Long id) {
        long childCount = count(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, id));
        if (childCount > 0) {
            throw new BizException(new IErrorCode() {
                @Override public String getCode() { return CommonErrorCode.BAD_REQUEST.getCode(); }
                @Override public String getMsg() { return "存在子菜单，无法删除"; }
            }, null);
        }
        removeById(id);
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

        // 只取目录和菜单（menuType=0），不取按钮（menuType=1）
        List<SysMenu> menus = list(new LambdaQueryWrapper<SysMenu>()
                .in(SysMenu::getId, menuIds)
                .ne(SysMenu::getMenuType, 1)
                .eq(SysMenu::getStatus, 1)
                .orderByAsc(SysMenu::getSort));

        // 补充父级菜单，确保树结构完整（避免子菜单失去父级节点）
        Set<Long> parentIds = menus.stream()
                .map(SysMenu::getParentId)
                .filter(pid -> pid != 0 && !menuIds.contains(pid))
                .collect(Collectors.toSet());
        if (!parentIds.isEmpty()) {
            menus.addAll(list(new LambdaQueryWrapper<SysMenu>()
                    .in(SysMenu::getId, parentIds)
                    .eq(SysMenu::getStatus, 1)));
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
                    menu.getMeta().setAffixTab(true);
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

        return VbenMenuResp.builder()
                .name(menu.getName())
                .path(menu.getPath())
                .component(component)
                .redirect(children.isEmpty() ? null : children.get(0).getPath())
                .meta(VbenMenuResp.Meta.builder()
                        .title(menu.getName())
                        .order(menu.getSort())
                        .icon(menu.getIcon())
                        .build())
                .children(childVOs.isEmpty() ? null : childVOs)
                .build();
    }

    /**
     * 实体转视图对象
     *
     * @param menu 菜单实体
     * @return 菜单视图对象
     */
    private SysMenuResp toVO(SysMenu menu) {
        return SysMenuResp.builder()
                .id(menu.getId())
                .parentId(menu.getParentId())
                .name(menu.getName())
                .path(menu.getPath())
                .component(menu.getComponent())
                .perms(menu.getPerms())
                .menuType(menu.getMenuType())
                .icon(menu.getIcon())
                .sort(menu.getSort())
                .status(menu.getStatus())
                .createTime(menu.getCreateTime())
                .children(new ArrayList<>())
                .build();
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
}
