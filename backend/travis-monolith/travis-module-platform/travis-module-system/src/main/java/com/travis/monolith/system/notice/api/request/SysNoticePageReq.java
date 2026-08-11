package com.travis.monolith.system.notice.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.common.web.model.PageRequest;
import com.travis.monolith.system.common.api.enums.PublishStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 公告分页查询参数。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysNoticePageReq extends PageRequest {

    /** 公告标题，支持模糊匹配。 */
    @Size(max = 255, message = "公告标题长度不能超过255个字符")
    private String title;

    /** 发布状态。 */
    @EnumValue(value = PublishStatus.class, message = "状态值错误")
    private Integer status;

    /** 发布日期范围起点。 */
    private LocalDate publishStartDate;

    /** 发布日期范围终点。 */
    private LocalDate publishEndDate;

    /** 发布日期起点不能晚于终点。 */
    @AssertTrue(message = "发布开始日期不能晚于结束日期")
    public boolean isPublishDateRangeValid() {
        return publishStartDate == null
                || publishEndDate == null
                || !publishStartDate.isAfter(publishEndDate);
    }
}
