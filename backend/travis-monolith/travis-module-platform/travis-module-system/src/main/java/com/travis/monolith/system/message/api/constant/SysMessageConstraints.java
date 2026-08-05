package com.travis.monolith.system.message.api.constant;

/** 消息字段及接收范围约束。 */
public final class SysMessageConstraints {
    public static final int TITLE_MAX_LENGTH = 255;
    public static final int CONTENT_MAX_LENGTH = 5000;
    public static final int JUMP_URL_MAX_LENGTH = 500;
    public static final int TEMPLATE_PARAMS_MAX_LENGTH = 4000;
    public static final int RECEIVER_VALUES_MAX_SIZE = 1000;

    private SysMessageConstraints() {}
}
