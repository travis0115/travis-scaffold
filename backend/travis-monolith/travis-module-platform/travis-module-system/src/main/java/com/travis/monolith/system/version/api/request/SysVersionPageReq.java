package com.travis.monolith.system.version.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.model.PageRequest;
import com.travis.monolith.system.common.api.enums.PublishStatus;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
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

    /** 版本号，支持模糊匹配。 */
    @Size(max = 50, message = "版本号长度不能超过50个字符")
    private String version;

    /** 版本标题，支持模糊匹配。 */
    @Size(max = 255, message = "标题长度不能超过255个字符")
    private String title;

    /** 发布状态。 */
    @EnumValue(value = PublishStatus.class, message = "状态值错误")
    private Integer status;

    /** 发布日期范围起点。 */
    private LocalDate publishStartDate;

    /** 发布日期范围终点。 */
    private LocalDate publishEndDate;
}
