package com.travis.infrastructure.common.web.exception;

/** 错误码对象接口 */
public interface ErrorCode {
    /** 状态码 */
    String getCode();

    /** 提示信息 */
    String getMsg();
}
