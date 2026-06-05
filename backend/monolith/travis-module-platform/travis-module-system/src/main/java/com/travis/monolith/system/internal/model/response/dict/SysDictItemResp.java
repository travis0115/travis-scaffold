package com.travis.monolith.system.internal.model.response.dict;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字典数据项视图，用于前端下拉框、单选框等组件的数据渲染
 *
 * @author travis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysDictItemResp {
    /** 字典项ID */
    private Long id;

    /** 所属字典类型ID */
    private Long dictId;

    /** 字典项标签（显示文本） */
    private String label;

    /** 字典项值（实际存储值） */
    private String value;

    /** 排序号 */
    private Integer sort;

    /** 状态（0-禁用 1-启用） */
    private Integer status;

    /** 备注 */
    private String remark;
}
