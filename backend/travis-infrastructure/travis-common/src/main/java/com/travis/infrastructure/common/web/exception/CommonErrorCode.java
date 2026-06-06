package com.travis.infrastructure.common.web.exception;

import lombok.AllArgsConstructor;

/** 全局通用状态码枚举 */
@AllArgsConstructor
public enum CommonErrorCode implements IErrorCode {
    /** 通用状态码 */
    SUCCESS("200", "操作成功"),

    /**
     * ──────────────────────────────────────────────────────────────── *
     * =========================== 客户端错误段 ========================= *
     * ──────────────────────────────────────────────────────────────── * 400-499：HTTP状态码
     * 1400-1499：自定义状态码
     */
    BAD_REQUEST("400", "请求参数不正确"),
    UNAUTHORIZED("401", "账号未登录"),
    FORBIDDEN("403", "权限不足"),
    NOT_FOUND("404", "请求资源不存在"),
    METHOD_NOT_ALLOWED("405", "不支持的请求方式"),
    LOCKED("423", "请求失败，请稍后重试"),
    UNSUPPORTED_MEDIA_TYPE("415", "不支持的 Content-Type"),
    TOO_MANY_REQUESTS("429", "请求过于频繁，请稍后重试"),
    REPEATED_REQUEST("1400", "重复请求，请稍后再试"),
    FILE_TOO_LARGE("1401", "上传文件大小超出限制，请调整后重试"),

    /**
     * ──────────────────────────────────────────────────────────────── *
     * =========================== 服务端错误段 ========================= *
     * ──────────────────────────────────────────────────────────────── * 500-599：HTTP状态码
     * 1500-1599：自定义状态码
     */
    INTERNAL_SERVER_ERROR("500", "发生错误"), // 服务器抛出异常

    INTERRUPTED("1500", "进程中断"),
    IO_EXCEPTION("1501", "IO异常"),
    ARITHMETIC_EXCEPTION("1502", "算数异常"),
    FILE_NOT_FOUND("1503", "文件或目录不存在"),
    FILE_UPLOAD_FAILED("1504", "文件上传失败"),

    /**
     * ──────────────────────────────────────────────────────────────── *
     * =========================== 自定义错误段 ========================= *
     * ──────────────────────────────────────────────────────────────── * 2000-2999
     */

    /** 参数校验 */
    VALIDATE_FAILED("2000", "参数校验失败：{0}"),
    VALIDATE_NUMBERFORMAT_EXCEPTION("2001", "参数类型非数字"),
    VALIDATE_METHOD_ARGUMENT_TYPE_MISMATCH("2002", "参数类型不匹配：{0}"),
    VALIDATE_MISSING_SERVLET_REQUEST_PARAMETER("2003", "请求参数缺失：{0}"),
    VALIDATE_MISSING_REQUIRED_REQUEST_BODY("2004", "请求参数类型错误，缺失：Request Body"),
    VALIDATE_MISSING_PATH_VARIABLE("2005", "请求路径参数缺失：{0}"),

    /** 数据库 */
    DATABASE_OPERATION_FAILED("2100", "数据库操作失败"),
    DATABASE_SELECT_NOT_FOUND("2101", "未找到记录"),
    DATABASE_INSERT_FAILED("2102", "新增记录失败"),
    DATABASE_UPDATE_FAILED("2103", "修改记录失败"),
    DATABASE_DELETE_FAILED("2104", "删除记录失败"),

    /** 缓存 */
    CACHE_OPERATION_FAILED("2200", "缓存操作失败"),
    CACHE_GET_FAILED("2201", "缓存获取失败"),
    CACHE_SET_FAILED("2202", "缓存设置失败"),
    CACHE_DELETE_FAILED("2203", "缓存删除失败"),
    CACHE_SET_EXPIRE_FAILED("2204", "缓存设置过期失败"),
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
