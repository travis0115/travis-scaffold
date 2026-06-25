package com.travis.monolith.ops.common.api;

/**
 * 系统权限常量
 *
 * @author travis
 */
public class OpsPermsConstant {
    private OpsPermsConstant() {}

    /*
     ──────────────────────────────────────────────────────────────── *
     ============================ 任务调度模块 ========================== *
     ──────────────────────────────────────────────────────────────── *
    */
    /** 任务调度查询权限 */
    public static final String OPS_JOB_QUERY = "ops:job:query";

    /** 任务调度更新权限 */
    public static final String OPS_JOB_UPDATE = "ops:job:update";

    /** 任务调度操作权限 */
    public static final String OPS_JOB_OPERATION = "ops:job:operation";

    /** 任务执行日志查询权限 */
    public static final String OPS_JOB_LOG_QUERY = "ops:job:log:query";
}
