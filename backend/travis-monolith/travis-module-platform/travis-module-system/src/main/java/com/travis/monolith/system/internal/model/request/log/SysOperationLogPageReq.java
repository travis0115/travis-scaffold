package com.travis.monolith.system.internal.model.request.log;

import com.travis.infrastructure.common.web.model.PageRequest;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作日志分页查询请求参数
 *
 * @author travis
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysOperationLogPageReq extends PageRequest {
    /** 操作用户名（模糊匹配） */
    private String username;

    /** 操作模块（模糊匹配） */
    private String module;

    /** 操作状态（0-失败 1-成功） */
    private Integer status;

    /** 操作开始时间 */
    private LocalDateTime startTime;

    /** 操作结束时间 */
    private LocalDateTime endTime;
}
