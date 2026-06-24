package com.travis.monolith.system.dict.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.monolith.system.common.api.constant.ValidationPattern;
import com.travis.monolith.system.common.api.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 字典类型新增请求参数
 *
 * @author travis
 */
@Data
public class SysDictCreateReq {
    /** 字典名称 */
    @NotBlank(message = "字典名称不能为空")
    @Size(min = 2, max = 20, message = "字典名称长度为2-20个字符")
    private String dictName;

    /** 字典编码（唯一标识） */
    @NotBlank(message = "字典编码不能为空")
    @Pattern(regexp = ValidationPattern.CODE, message = "字典编码必须以字母开头，只能包含字母、数字和下划线")
    @Size(max = 100, message = "字典编码长度不能超过100个字符")
    private String dictCode;

    /** 状态（0-禁用 1-启用） */
    @EnumValue(value = Status.class, message = "状态值错误")
    @NotNull(message = "状态值不允许为空")
    private Integer status;

    /** 备注 */
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;
}
