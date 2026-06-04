package com.travis.infrastructure.framework.satoken.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Web 配置属性
 *
 * @author travis
 */
@Data
@ConfigurationProperties(prefix = "travis.web.security")
public class SaTokenProperties {

    /**
     * 不校验登录的请求
     */
    private List<String> permitPaths = new ArrayList<>();


}
