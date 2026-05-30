package com.travis.monolith.system.internal.model.req;

import com.travis.infrastructure.framework.web.core.model.PageRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单新增/编辑请求参数
 *
 * @author travis
 */
@Data
public class SysMenuReq {
    /** 父菜单ID(0 表示顶级菜单) */
    @NotNull(message = "父菜单ID不能为空")
    private Long parentId;
    /** 菜单名称 */
    @NotBlank(message = "菜单名称不能为空")
    private String menuName;
    /** 路由路径 */
    private String path;
    /** 前端组件路径 */
    private String component;
    /** 权限标识（如 system:user:add） */
    private String perms;
    /** 菜单类型（0-目录 1-菜单 2-按钮） */
    @NotNull(message = "菜单类型不能为空")
    private Integer menuType;
    /** 图标 */
    private String icon;
    /** 排序号 */
    private Integer sort;
    /** 状态（0-禁用 1-启用） */
    private Integer status;
    /** 路由元信息JSON（Vben Admin RouteMeta 扩展字段） */
    private String meta;

    /**
     * 字典数据项新增/编辑请求参数
     *
     * @author travis
     */
    @Data
    public static class SysDictItemReq {
        /** 所属字典类型ID */
        @NotNull(message = "字典类型ID不能为空")
        private Long dictId;
        /** 字典项标签（显示文本） */
        @NotBlank(message = "字典标签不能为空")
        private String label;
        /** 字典项值（实际存储值） */
        @NotBlank(message = "字典值不能为空")
        private String value;
        /** 排序号 */
        private Integer sort;
        /** 状态（0-禁用 1-启用） */
        private Integer status;
        /** 备注 */
        private String remark;
    }

    /**
     * 字典类型新增/编辑请求参数
     *
     * @author travis
     */
    @Data
    public static class SysDictReq {
        /** 字典名称 */
        @NotBlank(message = "字典名称不能为空")
        private String dictName;
        /** 字典类型编码（唯一标识） */
        @NotBlank(message = "字典类型编码不能为空")
        private String dictType;
        /** 状态（0-禁用 1-启用） */
        private Integer status;
        /** 备注 */
        private String remark;
    }

    /**
     * 部门分页查询请求参数
     *
     * @author travis
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class SysDeptPageReq extends PageRequest {
        /** 部门名称（模糊匹配） */
        private String deptName;
        /** 状态（0-禁用 1-启用） */
        private Integer status;
    }

    /**
     * 部门新增/编辑请求参数
     *
     * @author travis
     */
    @Data
    public static class SysDeptReq {
        /** 父部门ID（0 表示顶级部门） */
        @NotNull(message = "父部门ID不能为空")
        private Long parentId;
        /** 部门名称 */
        @NotBlank(message = "部门名称不能为空")
        private String deptName;
        /** 排序号 */
        private Integer sort;
        /** 负责人 */
        private String leader;
        /** 联系电话 */
        private String phone;
        /** 状态（0-禁用 1-启用） */
        private Integer status;
    }
}
