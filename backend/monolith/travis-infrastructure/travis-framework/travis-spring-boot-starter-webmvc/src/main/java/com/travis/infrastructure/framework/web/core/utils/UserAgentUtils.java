package com.travis.infrastructure.framework.web.core.utils;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.travis.infrastructure.common.web.enums.PlatformType;
import com.travis.infrastructure.framework.web.core.model.UserAgentInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

import java.util.Map;

/**
 *
 * @author Travis
 */
@UtilityClass
public class UserAgentUtils {

    /**
     * 操作系统映射
     */
    private static final Map<String, String> OS_MAPPING = Map.of(
            "OSX", PlatformType.MACOS.getDisplayName(),
            "iPhone", PlatformType.IOS.getDisplayName(),
            "iPad", PlatformType.IPADOS.getDisplayName(),
            "Android", PlatformType.ANDROID.getDisplayName(),
            "Harmony", PlatformType.HARMONYOS.getDisplayName(),
            "Windows", PlatformType.WINDOWS.getDisplayName()
    );

    /**
     * 获取当前请求UA信息
     */
    public UserAgentInfo getCurrentUserAgentInfo() {
        String userAgent = ServletUtils.getUserAgent();
        return parse(userAgent);
    }

    /**
     * 获取当前请求UA信息
     */
    public UserAgentInfo getCurrentUserAgentInfo(HttpServletRequest request) {
        var userAgent = ServletUtils.getUserAgent(request);
        return parse(userAgent);
    }

    /**
     * 解析UA
     */
    public UserAgentInfo parse(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return UserAgentInfo.builder()
                    .browser("Unknown")
                    .os("Unknown")
                    .userAgent("")
                    .build();
        }
        UserAgent ua = UserAgentUtil.parse(userAgent);
        return UserAgentInfo.builder()
                .browser(buildBrowser(ua))
                .os(buildOs(ua))
                .userAgent(userAgent)
                .build();
    }

    private String buildBrowser(UserAgent ua) {
        if (ua == null || ua.getBrowser() == null) {
            return "Unknown";
        }
        String name = ua.getBrowser().getName();
        String version = ua.getVersion();

        if (version == null || version.isBlank()) {
            return name;
        }

        // 只保留主版本号
        String majorVersion = version.split("\\.")[0];
        return name + " " + majorVersion;
    }

    private String buildOs(UserAgent ua) {
        if (ua == null || ua.getOs() == null) {
            return "Unknown";
        }
        String os = ua.getOs().getName().split(" ")[0];
        return OS_MAPPING.getOrDefault(os, os);
    }

    /**
     * 获取浏览器
     */
    public String getBrowser() {
        return getCurrentUserAgentInfo().getBrowser();
    }

    /**
     * 获取操作系统
     */
    public String getOs() {
        return getCurrentUserAgentInfo().getOs();
    }

}