package com.travis.monolith.system.internal.converter;

import com.travis.monolith.system.internal.model.entity.SysDept;
import com.travis.monolith.system.internal.model.entity.SysDictItem;
import com.travis.monolith.system.internal.model.entity.SysMenu;
import com.travis.monolith.system.internal.model.entity.SysRole;
import com.travis.monolith.system.internal.model.entity.SysUpdateLog;
import com.travis.monolith.system.internal.model.entity.SysUser;
import com.travis.monolith.system.internal.model.req.SysMenuReq;
import com.travis.monolith.system.internal.model.req.SysRoleReq;
import com.travis.monolith.system.internal.model.req.SysUpdateLogReq;
import com.travis.monolith.system.internal.model.req.SysUserReq;
import com.travis.monolith.system.internal.model.resp.SysDeptResp;
import com.travis.monolith.system.internal.model.resp.SysDictItemResp;
import com.travis.monolith.system.internal.model.resp.SysMenuResp;
import com.travis.monolith.system.internal.model.resp.SysRoleResp;
import com.travis.monolith.system.internal.model.resp.SysUpdateLogResp;
import com.travis.monolith.system.internal.model.resp.SysUserResp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 系统模块对象转换器
 * 处理 Entity ↔ Req/Resp 之间的对象映射
 *
 * @author travis
 */
@Mapper(componentModel = "spring")
public interface SystemConverter {

    // ==================== 用户相关 ====================

    /**
     * SysUser → SysUserResp（基础字段映射）
     * deptName、roleIds、roleNames、lastLoginLocation 需在Service层手动设置
     */
    @Mapping(target = "deptName", ignore = true)
    @Mapping(target = "roleIds", ignore = true)
    @Mapping(target = "roleNames", ignore = true)
    @Mapping(target = "lastLoginLocation", ignore = true)
    SysUserResp toUserResp(SysUser user);

    List<SysUserResp> toUserRespList(List<SysUser> users);

    /**
     * SysUserReq → SysUser（新增时使用）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "invitationCode", ignore = true)
    @Mapping(target = "availableBalance", ignore = true)
    @Mapping(target = "lastLoginTime", ignore = true)
    @Mapping(target = "lastLoginIp", ignore = true)
    @Mapping(target = "version", ignore = true)
    SysUser toUserEntity(SysUserReq req);

    /**
     * SysUserReq → 更新已有的SysUser
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "invitationCode", ignore = true)
    @Mapping(target = "availableBalance", ignore = true)
    @Mapping(target = "lastLoginTime", ignore = true)
    @Mapping(target = "lastLoginIp", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateUserFromReq(SysUserReq req, @MappingTarget SysUser user);

    // ==================== 角色相关 ====================

    /**
     * SysRole → SysRoleResp（基础字段映射）
     * menuIds 需在Service层手动设置
     */
    @Mapping(target = "menuIds", ignore = true)
    SysRoleResp toRoleResp(SysRole role);

    List<SysRoleResp> toRoleRespList(List<SysRole> roles);

    /**
     * SysRoleReq → SysRole（新增时使用）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    SysRole toRoleEntity(SysRoleReq req);

    /**
     * SysRoleReq → 更新已有的SysRole
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateRoleFromReq(SysRoleReq req, @MappingTarget SysRole role);

    // ==================== 菜单相关 ====================

    /**
     * SysMenu → SysMenuResp（基础字段映射）
     * children 需在Service层手动设置
     */
    @Mapping(target = "children", ignore = true)
    SysMenuResp toMenuResp(SysMenu menu);

    List<SysMenuResp> toMenuRespList(List<SysMenu> menus);

    /**
     * SysMenuReq → SysMenu（新增时使用）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    SysMenu toMenuEntity(SysMenuReq req);

    /**
     * SysMenuReq → 更新已有的SysMenu
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateMenuFromReq(SysMenuReq req, @MappingTarget SysMenu menu);

    // ==================== 部门相关 ====================

    /**
     * SysDept → SysDeptResp（基础字段映射）
     * children 需在Service层手动设置
     */
    @Mapping(target = "children", ignore = true)
    SysDeptResp toDeptResp(SysDept dept);

    List<SysDeptResp> toDeptRespList(List<SysDept> depts);

    // ==================== 字典数据项相关 ====================

    /**
     * SysDictItem → SysDictItemResp（全部同名字段映射）
     */
    SysDictItemResp toDictItemResp(SysDictItem item);

    List<SysDictItemResp> toDictItemRespList(List<SysDictItem> items);

    // ==================== 更新日志相关 ====================

    /**
     * SysUpdateLog → SysUpdateLogResp（全部同名字段映射）
     */
    SysUpdateLogResp toUpdateLogResp(SysUpdateLog updateLog);

    List<SysUpdateLogResp> toUpdateLogRespList(List<SysUpdateLog> updateLogs);

    /**
     * SysUpdateLogReq → SysUpdateLog（新增时使用）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    SysUpdateLog toUpdateLogEntity(SysUpdateLogReq req);

    /**
     * SysUpdateLogReq → 更新已有的SysUpdateLog
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    void updateUpdateLogFromReq(SysUpdateLogReq req, @MappingTarget SysUpdateLog updateLog);
}
