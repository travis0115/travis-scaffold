package com.travis.infrastructure.framework.quartz.core;

/** 可由后台调度的任务处理器。处理器名称必须在应用内唯一。 */
public interface QuartzJobHandler {

    String getName();

    void execute(String params) throws Exception;
}
