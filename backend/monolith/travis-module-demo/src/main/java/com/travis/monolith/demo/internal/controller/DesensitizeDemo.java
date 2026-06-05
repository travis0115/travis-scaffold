package com.travis.monolith.demo.internal.controller;

import com.travis.infrastructure.framework.desensitize.core.annotation.RegexDesensitize;
import com.travis.infrastructure.framework.desensitize.core.annotation.SliderDesensitize;
import com.travis.infrastructure.framework.desensitize.core.annotation.regex.EmailDesensitize;
import com.travis.infrastructure.framework.desensitize.core.annotation.slider.*;
import lombok.Data;

/** 脱敏测试类 */
@Data
public class DesensitizeDemo {
    @ChineseNameDesensitize private String nickname;
    @BankCardDesensitize private String bankCard;
    @CarLicenseDesensitize private String carLicense;
    @FixedPhoneDesensitize private String fixedPhone;
    @IdCardDesensitize private String idCard;
    @PasswordDesensitize private String password;
    @MobileDesensitize private String phoneNumber;

    @SliderDesensitize(prefix = 3, suffix = 1, mask = '#')
    private String slider1;

    @SliderDesensitize(prefix = 3, suffix = 3)
    private String slider2;

    @SliderDesensitize(suffix = 1)
    private String slider3;

    @EmailDesensitize private String email;

    @RegexDesensitize(regex = "芋道源码", replacer = "***")
    private String regex;

    private String origin;
}
