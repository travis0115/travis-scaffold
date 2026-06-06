package com.travis.infrastructure.framework.web.core.model;

import lombok.Builder;
import lombok.Data;

/**
 * @author Travis
 */
@Data
@Builder
public class UserAgentInfo {
    /** 浏览器 */
    private String browser;

    /** 操作系统 */
    private String os;

    /** 原始UA */
    private String userAgent;
}
