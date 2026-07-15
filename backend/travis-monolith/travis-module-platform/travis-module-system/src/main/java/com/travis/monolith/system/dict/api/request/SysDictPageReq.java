package com.travis.monolith.system.dict.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.model.PageRequest;
import com.travis.monolith.system.common.api.enums.Status;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典分页查询请求参数
 *
 * @author travis
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysDictPageReq extends PageRequest {

    /** 字典名称，支持模糊匹配。 */
    @Size(max = 20, message = "字典名称长度不能超过20个字符")
    private String dictName;

    /** 字典编码，支持模糊匹配。 */
    @Size(max = 100, message = "字典编码长度不能超过100个字符")
    private String dictCode;

    /** 状态（0-禁用 1-启用） */
    @EnumValue(value = Status.class, message = "状态值错误")
    private Integer status;
}
