package com.travis.monolith.ops.job.internal.service;

import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.monolith.ops.job.api.OpsJobErrorCode;

/** 校验定时任务 JSON 参数。 */
public final class OpsJobParamValidator {

    private OpsJobParamValidator() {}

    /** 校验任务参数是否为合法 JSON。 */
    public static void validate(String params) {
        try {
            JsonUtil.getObjectMapper().readTree(params == null || params.isBlank() ? "{}" : params);
        } catch (Exception exception) {
            throw invalid("参数必须是合法 JSON");
        }
    }

    private static BizException invalid(String message) {
        return new BizException(OpsJobErrorCode.INVALID_PARAMS, message);
    }
}
