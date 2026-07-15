package com.travis.monolith.system.message.api.enums;

import com.travis.infrastructure.common.web.constant.LoginType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 消息接收端登录体系枚举。 */
@Getter
@AllArgsConstructor
public enum SysMessageReceiverType {
    /** 后台账号。 */
    ADMIN(LoginType.ADMIN),

    /** 客户端用户。 */
    APP(LoginType.APP);

    /** 接收端登录体系值。 */
    private final String value;
}
