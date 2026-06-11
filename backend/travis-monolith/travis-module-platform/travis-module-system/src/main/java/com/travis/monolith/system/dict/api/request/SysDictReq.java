package com.travis.monolith.system.dict.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 字典类型新增/编辑请求参数
 *
 * @author travis
 */
@Data
public class SysDictReq {
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
