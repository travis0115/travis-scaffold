package com.travis.infrastructure.framework.web.core.model;

import cn.hutool.core.lang.Assert;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.travis.infrastructure.framework.web.core.exception.CommonErrorCode;
import com.travis.infrastructure.framework.web.core.exception.IErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Api返回结果封装类
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    /**
     * 状态码
     *
     */
    private String code;

    /**
     * 提示信息
     */
    private String msg;

    /**
     * 提示信息参数
     */
    @JsonIgnore
    private Object[] args;

    /**
     * 数据封装
     */
    private T data;


    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> success(T data) {
        var result = new ApiResponse<T>();
        result.setCode(CommonErrorCode.SUCCESS.getCode());
        result.setMsg(CommonErrorCode.SUCCESS.getMsg());
        result.setData(data);
        return result;
    }

    public static <T> ApiResponse<T> error(IErrorCode errorCode, Object... args) {
        Assert.notEquals(CommonErrorCode.SUCCESS.getCode(), errorCode.getCode(), "Code 不能与成功时相同");
        var result = new ApiResponse<T>();
        result.setCode(errorCode.getCode());
        result.setMsg(errorCode.getMsg());
        result.setArgs(args);
        return result;
    }

}
