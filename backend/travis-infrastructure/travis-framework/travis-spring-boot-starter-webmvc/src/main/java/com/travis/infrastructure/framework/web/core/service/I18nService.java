package com.travis.infrastructure.framework.web.core.service;

import com.travis.infrastructure.common.web.exception.ErrorCode;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * 国际化服务
 *
 * @author travis
 */
public class I18nService {

    private static MessageSource messageSource;

    public I18nService(MessageSource messageSource) {
        I18nService.messageSource = messageSource;
    }

    /**
     * 获取国际化消息
     *
     * @param defaultMsg 当资源文件中不存在该键时，返回此msg
     */
    public String getMessage(String code, String defaultMsg) {
        return getMessage(code, null, defaultMsg);
    }

    /** 根据当前区域、消息编码和参数获取国际化文本。 */
    public String getMessage(String code, Object[] args, String defaultMsg) {
        var locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, args, defaultMsg, locale);
    }

    /** 根据统一错误码获取当前区域的国际化文本。 */
    public String getMessage(ErrorCode errorCode) {
        return getMessage(errorCode.getCode(), errorCode.getMsg());
    }
}
