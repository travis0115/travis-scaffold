package com.travis.monolith.system.version.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.model.PageRequest;
import com.travis.monolith.system.common.api.enums.Status;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统版本分页查询请求参数
 *
 * @author travis
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysVersionPageReq extends PageRequest {

    @Size(max = 50, message = "版本号长度不能超过50个字符")
    private String version;

    @Size(max = 255, message = "标题长度不能超过255个字符")
    private String title;

    @EnumValue(value = Status.class, message = "状态值错误")
    private Integer status;
}
