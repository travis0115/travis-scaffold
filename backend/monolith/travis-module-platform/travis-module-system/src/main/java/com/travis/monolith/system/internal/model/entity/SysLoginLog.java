package com.travis.monolith.system.internal.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志实体，对应 sys_login_log 表，记录每次登录的详细信息
 *
 * @author travis
 */
@Data
public class SysLoginLog implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID（雪花算法生成）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 登录用户名
     */
    private String username;
    /**
     * 登录IP地址
     */
    private String ip;
    /**
     * 登录地点（IP 解析）
     */
    private String location;
    /**
     * 浏览器类型
     */
    private String browser;
    /**
     * 操作系统
     */
    private String os;
    /**
     * 登录状态（0-失败 1-成功）
     */
    private Integer status;
    /**
     * 登录提示信息（成功/失败原因）
     */
    private String message;
    /**
     * 登录时间
     */
    private LocalDateTime loginTime;
}
