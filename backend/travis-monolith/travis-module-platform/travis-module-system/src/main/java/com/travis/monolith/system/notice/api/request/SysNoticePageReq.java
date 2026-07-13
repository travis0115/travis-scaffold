package com.travis.monolith.system.notice.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.model.PageRequest;
import com.travis.monolith.system.common.api.enums.PublishStatus;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysNoticePageReq extends PageRequest {

    @Size(max = 255, message = "公告标题长度不能超过255个字符")
    private String title;

    @EnumValue(value = PublishStatus.class, message = "状态值错误")
    private Integer status;

    private LocalDate publishStartDate;
    private LocalDate publishEndDate;
}
