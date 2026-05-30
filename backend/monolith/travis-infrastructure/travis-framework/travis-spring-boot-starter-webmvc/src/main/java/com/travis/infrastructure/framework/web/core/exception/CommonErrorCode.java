package com.travis.infrastructure.framework.web.core.exception;

import lombok.AllArgsConstructor;

/**
 * 全局通用状态码枚举
 */
@AllArgsConstructor
public enum CommonErrorCode implements IErrorCode {
    /**
     * 通用状态码
     */
    SUCCESS("200", "操作成功"),

    /**
     * ──────────────────────────────────────────────────────────────── *
     * =========================== 客户端错误段 ========================= *
     * ──────────────────────────────────────────────────────────────── *
     * 400-499：HTTP状态码
     * 1400-1499：自定义状态码
     */
    BAD_REQUEST("400", "请求参数不正确"),
    UNAUTHORIZED("401", "账号未登录"),
    FORBIDDEN("403", "权限不足"),
    NOT_FOUND("404", "资源不存在"),
    METHOD_NOT_ALLOWED("405", "不支持的请求方式"),
    LOCKED("423", "请求失败，请稍后重试"),    //并发请求
    TOO_MANY_REQUESTS("429", "请求过于频繁，请稍后重试"),
    REPEATED_REQUEST("1400", "重复请求，请稍后再试"),

    /**
     * ──────────────────────────────────────────────────────────────── *
     * =========================== 服务端错误段 ========================= *
     * ──────────────────────────────────────────────────────────────── *
     * 500-599：HTTP状态码
     * 1500-1599：自定义状态码
     */
    INTERNAL_SERVER_ERROR("500", "系统异常"),   //服务器抛出异常

    INTERRUPTED("1500", "进程中断"),
    IO_EXCEPTION("1501", "IO异常"),
    ARITHMETIC_EXCEPTION("1502", "算数异常"),

    /**
     * ──────────────────────────────────────────────────────────────── *
     * =========================== 自定义错误段 ========================= *
     * ──────────────────────────────────────────────────────────────── *
     * 2000-2999
     */
    UNKNOWN("2000", "未知错误"),

    /**
     * 参数校验
     */
    VALIDATE_FAILED("2100", "参数校验失败"),
    VALIDATE_NUMBERFORMAT_EXCEPTION("2101", "参数类型非数字"),
    VALIDATE_METHOD_ARGUMENT_TYPE_MISMATCH("2102", "参数类型不匹配"),
    VALIDATE_REQUEST_PARAMS_EMPTY("2103", "入参不能为空"),

    ;

    private final String code;

    private final String msg;


    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getMsg() {
        return this.msg;
    }
}
