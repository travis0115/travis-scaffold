package com.travis.monolith.system.file.api;

/** 文件引用检查扩展点，由引用文件的业务模块实现。 */
public interface SysFileReferenceChecker {

    /** 判断文件是否仍被业务数据引用。 */
    boolean isReferenced(Long fileId);
}
