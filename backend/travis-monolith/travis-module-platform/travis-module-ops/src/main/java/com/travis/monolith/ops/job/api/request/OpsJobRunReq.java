package com.travis.monolith.ops.job.api.request;

import lombok.Data;

/** 手动执行定时任务的参数。 */
@Data
public class OpsJobRunReq {
    /** 本次执行使用的参数；为空时使用任务默认参数。 */
    private String params;
}
