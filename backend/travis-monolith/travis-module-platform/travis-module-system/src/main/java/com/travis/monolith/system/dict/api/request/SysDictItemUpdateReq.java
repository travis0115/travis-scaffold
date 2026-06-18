package com.travis.monolith.system.dict.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.monolith.system.common.api.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 字典数据项修改修改请求参数
 *
 * @author travis
 */
@Data
public class SysDictItemUpdateReq {
    /** 所属字典类型ID */
    @NotNull(message = "字典类型ID不能为空")
    private Long dictId;

    /** 字典项标签（显示文本） */
    @NotBlank(message = "字典标签不能为空")
    private String label;

    /** 字典项值（实际存储值） */
    @NotBlank(message = "字典值不能为空")
    private String value;

    /** 展示样式 */
    private String tagStyle;

    /** 排序号 */
    private Integer sort;

    /** 状态（0-禁用 1-启用） */
    @EnumValue(value = Status.class, message = "状态值错误")
    @NotNull(message = "状态值不允许为空")
    private Integer status;

    /** 备注 */
    private String remark;
}
