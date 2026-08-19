package com.travis.monolith.ops.errorlog.api.response;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 错误日志详情响应。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysErrorLogDetailResp extends SysErrorLogResp {
    /** 最近发生明细，按发生时间倒序。 */
    private List<SysErrorLogOccurrenceResp> occurrences;
}
