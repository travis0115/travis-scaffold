package com.travis.monolith.system.notice.api.request;

import com.travis.infrastructure.common.validation.annotation.EnumValue;
import com.travis.infrastructure.framework.web.core.annotation.SanitizeHtml;
import com.travis.monolith.system.common.api.enums.IsPinned;
import jakarta.validation.constraints.*;
import lombok.Data;

/** 公告更新参数。 */
@Data
public class SysNoticeUpdateReq {
    /** 乐观锁版本号。 */
    @NotNull(message = "版本号不能为空")
    private Integer lockVersion;

    /** 公告标题。 */
    @NotBlank(message = "公告标题不能为空")
    @Size(max = 255, message = "公告标题长度不能超过255个字符")
    private String title;

    /** 公告 HTML 内容。 */
    @NotBlank(message = "公告内容不能为空")
    @SanitizeHtml
    @Size(max = 5000, message = "公告内容长度不能超过5000个字符")
    private String content;

    /** 是否置顶。 */
    @EnumValue(value = IsPinned.class, message = "置顶值错误")
    @NotNull(message = "置顶值不能为空")
    private Integer isPinned;

    /** 排序号。 */
    @NotNull(message = "排序号不能为空")
    @Min(value = 0, message = "排序号不能小于0")
    @Max(value = 9999, message = "排序号不能大于9999")
    private Integer sort;

    /** 备注。 */
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;
}
