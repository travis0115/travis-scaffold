package com.travis.monolith.system.internal.model.request.dept;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 部门新增/编辑请求参数
 *
 * @author travis
 */
@Data
public class SysDeptReq {
    /**
     * 父部门ID（0 表示顶级部门）
     */
    @NotNull(message = "父部门ID不能为空")
    private Long parentId;
    /**
     * 部门名称
     */
    @NotBlank(message = "部门名称不能为空")
    private String deptName;
    /**
     * 排序号
     */
    private Integer sort;
    /**
     * 负责人
     */
    private String leader;
    /**
     * 联系电话
     */
    private String mobile;
    /**
     * 状态（0-禁用 1-启用）
     */
    private Integer status;
}
