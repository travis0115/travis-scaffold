package com.travis.monolith.system.dept.api.request;

import com.travis.infrastructure.common.validation.annotation.In;
import com.travis.infrastructure.common.validation.annotation.Mobile;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 部门新增请求参数
 *
 * @author travis
 */
@Data
public class SysDeptCreateReq {
    /** 父部门ID（0 表示顶级部门） */
    private Long parentId;

    /** 部门名称 */
    @NotBlank(message = "部门名称不能为空")
    @Size(min = 2, max = 20, message = "部门名称长度为2-20个字符")
    private String deptName;

    /** 排序号 */
    @NotNull(message = "排序号不能为空")
    @Min(value = 0, message = "排序号不能小于0")
    @Max(value = 9999, message = "排序号不能大于9999")
    private Integer sort;

    /** 负责人 */
    private String leader;

    /** 联系电话 */
    @Mobile private String mobile;

    /** 状态（0-禁用 1-启用） */
    @In(
            value = {0, 1},
            message = "状态仅允许值为0或1")
    @NotNull(message = "状态值不允许为空")
    private Integer status;
}
