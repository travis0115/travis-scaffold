package com.travis.monolith.system.dict.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.monolith.system.common.api.enums.Status;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 字典数据项新增请求参数
 *
 * @author travis
 */
@Data
public class SysDictItemCreateReq {
    /** 所属字典类型ID */
    @NotNull(message = "字典类型ID不能为空")
    private Long dictId;

    /** 字典项标签（显示文本） */
    @NotBlank(message = "字典标签不能为空")
    @Size(max = 20, message = "字典标签长度不能超过20个字符")
    private String label;

    /** 字典项值（实际存储值） */
    @NotBlank(message = "字典值不能为空")
    @Size(max = 100, message = "字典值长度不能超过100个字符")
    private String value;

    /** 展示样式 */
    @Size(max = 100, message = "展示样式长度不能超过100个字符")
    private String tagStyle;

    /** 排序号 */
    @NotNull(message = "排序号不能为空")
    @Min(value = 0, message = "排序号不能小于0")
    @Max(value = 9999, message = "排序号不能大于9999")
    private Integer sort;

    /** 状态（0-禁用 1-启用） */
    @EnumValue(value = Status.class, message = "状态值错误")
    @NotNull(message = "状态值不允许为空")
    private Integer status;

    /** 备注 */
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;
}
