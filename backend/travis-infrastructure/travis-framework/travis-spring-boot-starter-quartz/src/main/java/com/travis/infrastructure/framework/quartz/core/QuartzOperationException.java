package com.travis.infrastructure.framework.quartz.core;

/** Quartz 底层操作失败。 */
public class QuartzOperationException extends RuntimeException {

    public QuartzOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
