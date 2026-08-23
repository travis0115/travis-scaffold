package com.travis.monolith.ops.job.internal.validator;

import com.travis.infrastructure.common.web.exception.BizException;
import com.travis.infrastructure.framework.jackson.core.JsonUtil;
import com.travis.monolith.ops.common.api.enums.OpsErrorCode;

/** 校验定时任务 JSON 参数。 */
public final class OpsJobParamValidator {

    private OpsJobParamValidator() {}

    /** 校验任务参数是否为合法 JSON。 */
    public static void validate(String params) {
        try {
            JsonUtil.getObjectMapper().readTree(params == null || params.isBlank() ? "{}" : params);
        } catch (Exception exception) {
            throw new BizException(OpsErrorCode.INVALID_PARAMS, "参数必须是合法 JSON");
        }
    }
}
