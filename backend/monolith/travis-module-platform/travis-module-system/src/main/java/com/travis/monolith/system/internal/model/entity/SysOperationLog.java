package com.travis.monolith.system.internal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志实体，对应 sys_operation_log 表，记录管理员的关键操作
 *
 * @author travis
 */
@Data
public class SysOperationLog implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 操作用户ID
     */
    private Long userId;
    /**
     * 操作用户名
     */
    private String username;
    /**
     * 操作描述
     */
    private String description;
    /**
     * 所属业务模块
     */
    private String module;
    /**
     * 请求方法全限定名
     */
    private String method;
    /**
     * 请求URL
     */
    private String requestUrl;
    /**
     * HTTP 请求方法（GET/POST/PUT/DELETE）
     */
    private String requestMethod;
    /**
     * 请求参数（JSON 格式）
     */
    private String requestParams;
    /**
     * 响应结果（JSON 格式）
     */
    private String responseResult;
    /**
     * 操作IP地址
     */
    private String ip;
    /**
     * 执行耗时（毫秒）
     */
    private Long duration;
    /**
     * 操作状态（0-失败 1-成功）
     */
    private Integer status;
    /**
     * 错误信息（失败时记录）
     */
    private String errorMsg;
    /**
     * 操作时间
     */
    private LocalDateTime createTime;
}
