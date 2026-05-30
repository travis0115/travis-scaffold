package com.travis.monolith.system.internal.model.resp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门树形结构视图，用于后台管理界面的部门管理
 *
 * @author travis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SysDeptResp {
    /** 部门ID */
    private Long id;
    /** 父部门ID */
    private Long parentId;
    /** 部门名称 */
    private String deptName;
    /** 排序号 */
    private Integer sort;
    /** 负责人 */
    private String leader;
    /** 联系电话 */
    private String phone;
    /** 状态（0-禁用 1-启用） */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 子部门列表 */
    private List<SysDeptResp> children;
}
