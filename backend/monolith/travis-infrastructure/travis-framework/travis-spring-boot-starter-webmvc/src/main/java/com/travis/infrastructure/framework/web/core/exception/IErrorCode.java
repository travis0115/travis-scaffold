package com.travis.infrastructure.framework.web.core.exception;

/**
 * 错误码对象接口
 */
public interface IErrorCode {
    /**
     * 状态码
     */
    String getCode();

    /**
     * 提示信息
     */
    String getMsg();
}
