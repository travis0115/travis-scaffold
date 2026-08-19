package com.travis.infrastructure.framework.quartz.core;

/** 可由后台调度的任务处理器。处理器名称必须在应用内唯一。 */
public interface QuartzJobHandler {

    String getName();

    /** 处理器用途说明，用于任务配置界面展示。 */
    default String getDescription() {
        return "";
    }

    /** 是否为仅供系统内置任务使用的处理器。 */
    default boolean isBuiltin() {
        return false;
    }

    void execute(String params) throws Exception;
}
